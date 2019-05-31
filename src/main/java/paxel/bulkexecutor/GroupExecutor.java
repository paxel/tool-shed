package paxel.bulkexecutor;

import paxel.bulkexecutor.internal.SingleSourceSequentialProcessor;
import java.util.concurrent.ExecutorService;
import paxel.bulkexecutor.internal.MultiSourceSequentialProcessor;

public class GroupExecutor {

    private final ExecutorService executorService;

    public GroupExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * This creates an unbounded SequentialProcessor, for singleThreaded Input.
     *
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createSingleSourceSequentialProcessor() {
        return new SingleSourceSequentialProcessor(executorService);
    }

    /**
     * This creates an bounded SequentialProcessor, for singleThreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted.
     *
     * @param limit The limit for unprocessed runnables.
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createSingleSourceBoundedSequentialProcessor(int limit) {
        return new SingleSourceSequentialProcessor(executorService, limit);
    }

    /**
     * This creates an unbounded SequentialProcessor, for multithreaded Input.
     * The input performance gets worse, the more threads try to add Runnables.
     *
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createMultiSourceSequentialProcessor() {
        return new MultiSourceSequentialProcessor(executorService);
    }

    /**
     * This creates an bounded SequentialProcessor, for multithreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted. The input performance gets worse, the more
     * threads try to add Runnables.
     *
     * @param limit The limit for unprocessed runnables.
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createMultiSourceBoundedSequentialProcessor(int limit) {
        return new MultiSourceSequentialProcessor(executorService, limit);
    }
}
