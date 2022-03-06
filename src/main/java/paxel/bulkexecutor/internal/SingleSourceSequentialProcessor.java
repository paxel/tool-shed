package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import paxel.bulkexecutor.RunnableCompleter;
import paxel.bulkexecutor.SequentialProcessor;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This {@link SequentialProcessor} is safe to use in a single Thread. All
 * {@link Runnable}s are executed by the executor service in the order as the
 * are added.
 */
public class SingleSourceSequentialProcessor implements SequentialProcessor {

    private static final int IDLE = 0;
    private static final int QUEUED = 1;
    private static final int FINISHED = 2;
    private static final int ABORT = 3;

    private final Queue<Runnable> queue;
    private final ExecutorService executorService;
    private final ErrorHandler errorHandler;
    private final QueueBatchRunner queueRunner;
    private volatile int runStatus;

    /**
     * Constructs an instance with an unbound queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param errorHandler    the handler that decides if the Processor continues
     *                        in case a Runnable failed.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler) {
        queue = new ConcurrentLinkedQueue<>();
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, 1);
        this.errorHandler = errorHandler;
    }

    /**
     * Constructs an instance with a limited queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param errorHandler    the handler that decides if the Processor continues
     *                        in case a Runnable failed.
     * @param limit           The limit of the input queue.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler, int limit) {
        queue = new LinkedBlockingQueue(limit);
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, 1);
        this.errorHandler = errorHandler;
    }

    /**
     * Constructs an instance with an unbound queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param batch           The maximum number of messages to be processed before releasing the thread
     * @param errorHandler    the handler that decides if the Processor continues
     *                        in case a Runnable failed.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler) {
        queue = new ConcurrentLinkedQueue<>();
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, batch);
        this.errorHandler = errorHandler;
    }

    /**
     * Constructs an instance with a limited queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param batch           The maximum number of messages to be processed before releasing the thread
     * @param errorHandler    the handler that decides if the Processor continues
     *                        in case a Runnable failed.
     * @param limit           The limit of the input queue.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler, int limit) {
        queue = new LinkedBlockingQueue(limit);
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, batch);
        this.errorHandler = errorHandler;
    }

    /**
     * Adds a new Runnable to the process Queue. Will return true if that was
     * successful or false if the Queue is full or the Processor was aborted.
     *
     * @param r the new Runnable.
     * @return true if successful.
     */
    @Override
    public boolean add(Runnable r) {

        if (runStatus == ABORT) {
            // We don't accept any new Runnables.
            return false;
        }
        // first we try to put the runnable in the queue
        final boolean offer = queue.offer(r);
        // local copy to prevent changes in between.
        int runStatusAfterAfterOffer = this.runStatus;
        if (offer) {
            for (; ; ) {
                switch (runStatusAfterAfterOffer) {
                    case IDLE: {
                        /**
                         * There is currently no QueueRunner active for this
                         * Processor. We change the status and submit a QueueRunner.
                         */
                        this.runStatus = QUEUED;
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        // when the QueueRunner is finished, the finished method will be executed.
                        // adding the handle method before the submit makes sure the fiinished method is always called by the executor framework
                        future.handle((a, b) -> finished(b));
                        executorService.submit(new RunnableCompleter(queueRunner, future));
                        return true;
                    }
                    case QUEUED: {
                        /**
                         * There is currently a QueueRunner active.
                         * Either it already has grabbed our runnable or it will
                         * after it has finished.
                         */
                        return true;
                    }
                    case ABORT: {
                        // we don't accept any new jobs
                        queue.clear();
                        return false;
                    }
                    case FINISHED:
                    default: {
                        // busy loop. the QueueRunner just finished and has to decide on the runStatus
                        runStatusAfterAfterOffer = this.runStatus;
                    }
                }
            }
        }
        return false;
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
        // we mark that we are finished and have to decide what we do next.
        runStatus = FINISHED;
        if (ex != null) {
            // The Runnable failed and we must let the Errorhandler decide if we abort.
            if (!errorHandler.check(ex)) {
                // The errorhandler aborts processing. we clear the queue and set on abort to avoid accepting new jobs
                this.runStatus = ABORT;
                queue.clear();
                return null;
            }
        }
        // the job has finished (successfully). If something is still in the queue, we enqueue a new QueueRunner.
        if (!queue.isEmpty()) {
            // mark queued after check
            runStatus = QUEUED;
            // we submit the QueueRunner again.
            CompletableFuture<Void> future = new CompletableFuture<>();
            executorService.submit(new RunnableCompleter(queueRunner, future));
            // and will go back into the finished method when it completes
            future.handle((a, b) -> finished(b));
        } else {
            // nothing to do. we go idle.
            this.runStatus = IDLE;
        }
        return null;
    }
}
