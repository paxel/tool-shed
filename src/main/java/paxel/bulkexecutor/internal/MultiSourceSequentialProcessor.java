package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class MultiSourceSequentialProcessor extends SingleSourceSequentialProcessor {

    private final ReentrantLock in = new ReentrantLock();

    public MultiSourceSequentialProcessor(ExecutorService executorService, int batch, ErrorHandler errorHandler, int limit, boolean blocking) {
        super(executorService, batch, errorHandler, limit, blocking);
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
