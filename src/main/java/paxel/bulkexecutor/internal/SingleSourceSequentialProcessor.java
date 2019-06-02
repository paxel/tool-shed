package paxel.bulkexecutor.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import paxel.bulkexecutor.FutureRunnable;
import paxel.bulkexecutor.SequentialProcessor;

public class SingleSourceSequentialProcessor implements SequentialProcessor {

    private final LinkedBlockingQueue<Runnable> blockingQueue;
    private final ExecutorService executorService;
    private volatile Status status = Status.IDLE;
    private ReentrantLock lock = new ReentrantLock();
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
            if (status == Status.IDLE) {
                // we have to activate this again
                // this path is critial!
                // there is the posibilityy that the last job just checked the queue and set the status to active.
                // so to be on the save side we take a lock and check again.
                // we must not submit the same runner concurrently!
                lock.lock();
                try {
                    if (status == Status.IDLE) {
                        status = Status.ACTIVE;
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        executorService.submit(new FutureRunnable(queueRunner, future));
                        future.handle(this::finished);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return offer;
    }

    @Override
    public int size() {
        return blockingQueue.size();
    }

    private Void finished(Void ignorable, Throwable ex) {

        if (ex != null) {
            // TODO: errorhandler
        }
        // the job has finished.
        if (blockingQueue.isEmpty()) {
            // this path is critically
            // maybe the add just added a queue and checked if we are still active
            // to be safe we take the lock and check if the queue is really empty
            lock.lock();
            try {
                if (blockingQueue.isEmpty()) {
                    // all jobs done. wait for another job
                    status = Status.IDLE;
                    return null;
                }
            } finally {
                lock.unlock();
            }
        }
        // The queue is not empty, and if the runner will finish immediately
        CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.submit(new FutureRunnable(queueRunner, future));
        future.handle(this::finished);
        return null;
    }

    private static enum Status {
        IDLE, ACTIVE
    }

}
