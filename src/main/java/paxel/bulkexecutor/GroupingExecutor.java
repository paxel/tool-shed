package paxel.bulkexecutor;

import java.util.concurrent.ExecutorService;

import paxel.bulkexecutor.internal.ReusableSequentialProcessorBuilder;

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
     * Creates a SequentialProcessorBuilder that can be used to create different {@link SequentialProcessor} instances.
     *
     * @return the builder.
     */
    public SequentialProcessorBuilder create() {
        return new ReusableSequentialProcessorBuilder(executorService);
    }
}
