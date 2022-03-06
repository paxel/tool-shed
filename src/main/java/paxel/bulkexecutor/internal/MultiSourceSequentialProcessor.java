package paxel.bulkexecutor.internal;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import paxel.bulkexecutor.ErrorHandler;

public class MultiSourceSequentialProcessor extends SingleSourceSequentialProcessor {

    private final ReentrantLock in = new ReentrantLock();

    public MultiSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler) {
        super(executorService, errorHandler);
    }

    public MultiSourceSequentialProcessor(ExecutorService executorService, ErrorHandler errorHandler, int limit) {
        super(executorService, errorHandler, limit);
    }

    public MultiSourceSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler) {
        super(executorService, batch, errorHandler);
    }

    public MultiSourceSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler, int limit) {
        super(executorService, batch, errorHandler, limit);
    }

    @Override
    public boolean add(Runnable r) {
        // we have to make sure that not two threads simultaneously toggle the internal status.
        in.lock();
        try {
            return super.add(r);
        } finally {
            in.unlock();
        }

    }

}
