package paxel.bulkexecutor;

/**
 * The Sequential Processor will process all given Runnables sequentially. After
 * all Runnables are processed, the Processor will become idle until a new
 * Runnable is added.
 *
 * This works basically like this: The SequentialProcessor enqueues a job to a
 * central Executor, as soon as it gets a Runnable added. When more runnables
 * are added, they go to a queue, if there is already a job active. When the job
 * finishes, it checks if there are more runnables, and the limit is not
 * reached, and processes them immediately. Otherwise it creates a new job, and
 * enqueues it to the central executor, giving possible other Processors
 * processing time. If there are no Runnables, the processor does not use any
 * CPU cycles.
 */
public interface SequentialProcessor {

    /**
     * Adds a new Runnable to the processing queue.
     *
     * @param r The new runnable
     * @return true if the runnable was accepted.
     */
    boolean add(Runnable r);

    /**
     * The current size of the Queue. Depending on the queue size, this call
     * might be expensive.
     *
     * @return size.
     */
    int size();

    /**
     * This returns as soon the Processor is (temporarily) Idle or aborted due
     * to an Error.
     *
     * @throws InterruptedException in case the Thread got interrupted.
     */
    void awaitFinish() throws InterruptedException;

    /**
     * The Processor was aborted bcause a Runnable threw a Throwable and the
     * ErrorHandler decided to abort the process. The remaining Runnables were
     * removed from the queue.
     *
     * @return {@code true} in case the Processor aborted.
     */
    boolean isAborted();

    /**
     * The processor has currently no Runnables to execute.
     *
     * @return {@code true} if idle.
     */
    boolean isIdle();
}
