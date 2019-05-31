package paxel.bulkexecutor.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public class MultiSourceSequentialProcessor extends SingleSourceSequentialProcessor {

    private final ReentrantLock in = new ReentrantLock();

    public MultiSourceSequentialProcessor(ExecutorService executorService) {
        super(executorService);
    }

    public MultiSourceSequentialProcessor(ExecutorService executorService, int limit) {
        super(executorService, limit);
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
