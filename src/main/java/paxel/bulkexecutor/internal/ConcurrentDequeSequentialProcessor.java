package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import paxel.bulkexecutor.RunnableCompleter;
import paxel.bulkexecutor.SequentialProcessor;
import sun.misc.Unsafe;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("BusyWait")
public class ConcurrentDequeSequentialProcessor implements SequentialProcessor {
    private static final int IDLE = 0;
    private static final int QUEUED = 1;
    private static final int FINISHED = 2;
    private static final int ABORT = 3;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition full = reentrantLock.newCondition();

    private final AtomicInteger backPressure = new AtomicInteger();
    private final ConcurrentLinkedDeque<Runnable> queue;
    private final ExecutorService executorService;
    private final ErrorHandler errorHandler;
    private final QueueBatchRunner queueRunner;
    private volatile int runStatus;

    public ConcurrentDequeSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler) {
        queue = new ConcurrentLinkedDeque<>();
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, batch, this::notifyBackPressure);
        this.errorHandler = errorHandler;
    }

    private void notifyBackPressure() {
        try {
            reentrantLock.lock();
            if (backPressure.get() > 0)
                full.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Adds a new Runnable to the process Queue. Will return true if that was
     * successful or false if the Queue is full or the Processor was aborted.
     *
     * @param r the new Runnable.
     */
    @Override
    public void add(Runnable r) {
        addWithOptionalBackPressure(r, null);
    }

    @Override
    public boolean addWithBackPressure(Runnable runnable, int threshold) {
        return addWithOptionalBackPressure(runnable, threshold);
    }

    private boolean addWithOptionalBackPressure(Runnable r, Integer threshold) {
        if (runStatus == ABORT) {
            // We don't accept any new Runnable.
            return false;
        }
        // first, we try to put the runnable in the queue

        if (threshold != null) {
           try{
               awaitQueueSize(threshold);
           } catch (InterruptedException e) {
               // Sneaky throw
               Unsafe.getUnsafe().throwException(e);
           }
        }
        try {
            // in case two sources add messages concurrently; we must make sure the status is always correct
            // otherwise we could have a state where messages are queued, but no runner active
            reentrantLock.lock();
            boolean offer = queue.offer(r);
            // local copy to prevent changes in between.
            var runStatusAfterAfterOffer = this.runStatus;
            if (offer) {
                for (; ; ) {
                    switch (runStatusAfterAfterOffer) {
                        case IDLE -> {
                            /*
                             * There is currently no QueueRunner active for this
                             * Processor. We change the status and submit a QueueRunner.
                             */
                            this.runStatus = QUEUED;
                            final var future = new CompletableFuture<Void>();
                            // when the QueueRunner is finished, the finished method will be executed.
                            // adding the handle method before the submission makes sure the finished method is
                            // always called by the executor framework
                            future.handle((a, b) -> finished(b));
                            executorService.submit(new RunnableCompleter(queueRunner, future));
                            return true;
                        }
                        case QUEUED -> {
                            /*
                             * There is currently a QueueRunner active.
                             * Either it already has grabbed our runnable or it will
                             * after it has finished.
                             */
                            return true;
                        }
                        case ABORT -> {
                            // we don't accept any new jobs
                            queue.clear();
                            return false;
                        }
                        default -> {
                            // busy loop. the QueueRunner just finished and has to decide on the runStatus
                            runStatusAfterAfterOffer = this.runStatus;
                        }
                    }
                }
            }
            return false;
        } finally {
            reentrantLock.unlock();
        }
    }

    private void awaitQueueSize(Integer threshold) throws InterruptedException {
        try {
            reentrantLock.lock();
            backPressure.incrementAndGet();
            while (queue.size() > threshold) {
                full.await();
            }
        } finally {
            backPressure.decrementAndGet();
            reentrantLock.unlock();
        }
    }


    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isIdle() {
        return runStatus == IDLE;
    }

    @Override
    public boolean isAborted() {
        return runStatus == ABORT;
    }

    @Override
    public void awaitFinish() throws InterruptedException {
        while (!isIdle() && !isAborted()) {
            Thread.sleep(100);
        }
    }

    private Void finished(Throwable ex) {
        try {
            reentrantLock.lock();
            // we mark that we are finished and have to decide what we do next.
            runStatus = FINISHED;
            if (ex != null && !errorHandler.check(ex)) {
                // The errorhandler aborts processing.
                // We clear the queue and set on abort to
                // avoid accepting new jobs
                this.runStatus = ABORT;
                queue.clear();
                return null;
            }
            /*
             The job has finished (successfully).
             If something is still in the queue, we enqueue a new QueueRunner.
            */
            if (!queue.isEmpty()) {
                // mark queued after check
                runStatus = QUEUED;
                // we submit the QueueRunner again.
                final var future = new CompletableFuture<Void>();
                executorService.submit(new RunnableCompleter(queueRunner, future));
                // and will go back into the finished method when it completes
                future.handle((a, b) -> finished(b));
            } else {
                // nothing to do. we go idle.
                this.runStatus = IDLE;
            }
            return null;
        } finally {
            reentrantLock.unlock();
        }
    }

}
