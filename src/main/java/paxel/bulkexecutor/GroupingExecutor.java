package paxel.bulkexecutor;

import paxel.bulkexecutor.internal.ReusableSequentialProcessorBuilder;
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
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createSingleSourceSequentialProcessor() {
        return new SingleSourceSequentialProcessor(executorService, DEFAULT);
    }

    /**
     * This creates an bounded SequentialProcessor, for singleThreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @param limit The limit for unprocessed runnables.
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createSingleSourceBoundedSequentialProcessor(int limit) {
        return new SingleSourceSequentialProcessor(executorService, DEFAULT, limit);
    }

    /**
     * This creates an unbounded SequentialProcessor, for multithreaded Input.
     * The input performance gets worse, the more threads try to add Runnables.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createMultiSourceSequentialProcessor() {
        return new MultiSourceSequentialProcessor(executorService, DEFAULT);
    }

    /**
     * This creates an bounded SequentialProcessor, for multithreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted. The input performance gets worse, the more
     * threads try to add Runnables.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @param limit The limit for unprocessed runnables.
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createMultiSourceBoundedSequentialProcessor(int limit) {
        return new MultiSourceSequentialProcessor(executorService, DEFAULT, limit);
    }


    /**
     * This creates an unbounded SequentialProcessor, for singleThreaded Input.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createSingleSourceSequentialProcessor(ErrorHandler errorHandler) {
        return new SingleSourceSequentialProcessor(executorService, errorHandler);
    }

    /**
     * This creates an bounded SequentialProcessor, for singleThreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @param limit The limit for unprocessed runnables.
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createSingleSourceBoundedSequentialProcessor(int limit, ErrorHandler errorHandler) {
        return new SingleSourceSequentialProcessor(executorService, errorHandler, limit);
    }

    /**
     * This creates an unbounded SequentialProcessor, for multithreaded Input.
     * The input performance gets worse, the more threads try to add Runnables.
     *
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createMultiSourceSequentialProcessor(ErrorHandler errorHandler) {
        return new MultiSourceSequentialProcessor(executorService, errorHandler);
    }

    /**
     * This creates an bounded SequentialProcessor, for multithreaded Input. In
     * case the number of unprocessed Runnables reaches the given limit, no more
     * Runnables are excepted. The input performance gets worse, the more
     * threads try to add Runnables.
     * @deprecated use {@link #create()} instead to create a builder for creating a {@link SequentialProcessor}
     * @param limit        The limit for unprocessed runnables.
     * @param errorHandler the handler for Exceptions that happen while processing a Runnable.
     * @return the SequentialProcessor.
     */
    @Deprecated()
    public SequentialProcessor createMultiSourceBoundedSequentialProcessor(int limit, ErrorHandler errorHandler) {
        return new MultiSourceSequentialProcessor(executorService, errorHandler, limit);
    }

    /**
     * Creates a SequentialProcessorBuilder that can be used to create different {@link SequentialProcessor} instances.
     *
     * @return the builder.
     */
    public SequentialProcessorBuilder create() {
        return new ReusableSequentialProcessorBuilder(executorService);
    }
}
