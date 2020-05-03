package paxel.bulkexecutor;

import paxel.bulkexecutor.internal.SingleSourceSequentialProcessor;
import java.util.concurrent.ExecutorService;
import paxel.bulkexecutor.internal.MultiSourceSequentialProcessor;

/**
 * The Grouping Executor provides SequentialProcessors that process Runnables
 * sequentially per Processor. A Processor that never receives Runnables, will
 * never be executed, thus it is safe to create many Processors without wasting
 * too many resources. A typical use case would be an Actor System, where every
 * actor has one Processor. The actor lets the Processor execute the actors
 * actions and it is guaranteed, that no two processes of this actor run
 * parallel.
 *
 */
public class GroupingExecutor {

    private final ErrorHandler DEFAULT = x -> true;
    private final ExecutorService executorService;

    /**
     * Constructs the Grouping Executor with an Executor service that defines
     * how many {@link SequentialProcessor} are concurrently active.
     *
     * @param executorService The used executor
     */
    public GroupingExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * This creates an unbounded SequentialProcessor, for singleThreaded Input.
     *
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createSingleSourceSequentialProcessor() {
        return new SingleSourceSequentialProcessor(executorService, DEFAULT);
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
        return new SingleSourceSequentialProcessor(executorService, DEFAULT, limit);
    }

    /**
     * This creates an unbounded SequentialProcessor, for multithreaded Input.
     * The input performance gets worse, the more threads try to add Runnables.
     *
     * @return the SequentialProcessor.
     */
    public SequentialProcessor createMultiSourceSequentialProcessor() {
        return new MultiSourceSequentialProcessor(executorService, DEFAULT);
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
        return new MultiSourceSequentialProcessor(executorService, DEFAULT, limit);
    }
}
