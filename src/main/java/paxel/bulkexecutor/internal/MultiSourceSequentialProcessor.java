package paxel.bulkexecutor.internal;

import java.util.concurrent.ExecutorService;
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

    @Override
    public boolean add(Runnable r) {
        // we have to make sure that not two threads simultanously toggle the status
        in.lock();
        try {
            return super.add(r);
        } finally {
            in.unlock();
        }

    }

}
