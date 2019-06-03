package paxel.bulkexecutor.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import paxel.bulkexecutor.FutureRunnable;
import paxel.bulkexecutor.SequentialProcessor;

public class SingleSourceSequentialProcessor implements SequentialProcessor {

    private static final int IDLE = 0;
    private static final int QUEUED = 1;
    private static final int FINISHED = 2;
    private final LinkedBlockingQueue<Runnable> blockingQueue;
    private final ExecutorService executorService;
    private volatile int runStatus;
    private volatile int added;
    private QueueRunner queueRunner;

    public SingleSourceSequentialProcessor(ExecutorService executorService) {
        blockingQueue = new LinkedBlockingQueue<>();
        this.executorService = executorService;
        this.queueRunner = new QueueRunner(blockingQueue, 1);
    }

    public SingleSourceSequentialProcessor(ExecutorService executorService, int limit) {
        blockingQueue = new LinkedBlockingQueue(limit);
        this.executorService = executorService;
    }

    @Override
    public boolean add(Runnable r) {
        // only one thread calls this add, so we don't have to check for that
        final boolean offer = blockingQueue.offer(r);
        if (offer) {
            // added is the number of added runnables (until overflow but we only check for equal, not bigger than) 
            added += 1;
            for (;;) {
                if (runStatus == IDLE) {
                    // this thing is not added. the add method is the only one to add it, so it's save to add
                    runStatus = 1;
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    executorService.submit(new FutureRunnable(queueRunner, future));
                    future.handle((a, b) -> finished(a, b, added));
                    break;
                } else if (runStatus == QUEUED) {
                    // its currently queued, it will enqueue itself later
                    break;
                }
                // else: it's currently finished. it will either queue or idle soon 
            }
        }
        return offer;
    }

    @Override
    public int size() {
        return blockingQueue.size();
    }

    private Void finished(Void ignorable, Throwable ex, int myAdd) {
        runStatus = FINISHED;
        if (ex != null) {
            // TODO: errorhandler
        }
        // the job has finished. if inbetween something was added, we have to enqueue. or if there is something in the queue.
        if (myAdd != added || !blockingQueue.isEmpty()) {
            runStatus = QUEUED;
            // The queue is not empty, and if the runner will finish immediately
            CompletableFuture<Void> future = new CompletableFuture<>();
            executorService.submit(new FutureRunnable(queueRunner, future));
            future.handle((a, b) -> finished(a, b, this.added));
        } else {
            // queue
            this.runStatus = IDLE;
        }
        return null;
    }
}
