package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import paxel.bulkexecutor.RunnableCompleter;
import paxel.bulkexecutor.SequentialProcessor;

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
    private volatile int runStatus;
    private volatile int added;
    private QueueBatchRunner queueRunner;

    /**
     * Constructs an instance with an unbound queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param errorHandler the handler that decides if the Processor continues
     * in case a Runnable failed.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler) {
        queue = new ConcurrentLinkedDeque<>();
        this.executorService = executorService;
        this.queueRunner = new QueueBatchRunner(queue, 1);
        this.errorHandler = errorHandler;
    }

    /**
     * Constructs an instance with a limited queue.
     *
     * @param executorService The executor that runs the {@link Runnable}s.
     * @param errorHandler the handler that decides if the Processor continues
     * in case a Runnable failed.
     * @param limit The limit of the input queue.
     */
    public SingleSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler, int limit) {
        queue = new LinkedBlockingQueue(limit);
        this.executorService = executorService;
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
        // only one thread calls this add, so we don't have to check for that.
        // first we try to put the runnable in the queue
        final boolean offer = queue.offer(r);
        if (offer) {
            /**
             * we managed to put the runnable in the queue. we increment the
             * number of runnables added. This number helps the future to
             * determine if something has changed, since we queued the
             * QueueRunner.
             */
            added += 1;
            for (;;) {
                if (runStatus == IDLE) {
                    /**
                     * There is currently no QueueRunner active for this
                     * Processor. We change the status and submit a QueueRunner.
                     */
                    runStatus = QUEUED;
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    executorService.submit(new RunnableCompleter(queueRunner, future));
                    // when the QueueRunner is finished, the finished method will be executed.
                    future.handle((a, b) -> finished(a, b, added));
                    break;
                } else if (runStatus == QUEUED) {
                    /**
                     * There is currently a QueueRunner active. We have
                     * incremented added, so he will queue a new QueueRunner
                     * after it has finished.
                     */
                    break;
                }
                if (runStatus == ABORT) {
                    // we don't accept any new jobs
                    queue.clear();
                    return false;
                }
                // else: busy loop. the QueueRunner just finished and has to decide on the runStatus
            }
        }
        return offer;
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

    private Void finished(Void ignorable, Throwable ex, int myAdd) {
        // we mark that we are finished and have to decide what we do next.
        runStatus = FINISHED;
        if (ex != null) {
            // The Runnable failed and we must let the Errorhandler decide if we abort.
            if (!errorHandler.check(ex)) {
                // Th errorhandler aborts processing. we clear the queue and set on abort to avoid accepting new jobs
                this.runStatus = ABORT;
                queue.clear();
                return null;
            }
        }
        // the job has finished successfully. If inbetween something was added, we enqueue a new QueueRunner.
        // currently only a single job queue runner is used, and thus the queue is not empty if myAdd!=added
        // The queue can contain Runnables even if nothing was added.
        if (myAdd != added || !queue.isEmpty()) {
            runStatus = QUEUED;
            // we submit the QueueRunner again.
            CompletableFuture<Void> future = new CompletableFuture<>();
            executorService.submit(new RunnableCompleter(queueRunner, future));
            // and will go back into the finished method when it completes
            future.handle((a, b) -> finished(a, b, this.added));
        } else {
            // nothing to do. we go idle.
            this.runStatus = IDLE;
        }
        return null;
    }
}
