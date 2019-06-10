package paxel.bulkexecutor;

/**
 * The Sequential Processor will process all given Runnables sequentially. After
 * all Runnables are processed, the Processor will become idle until a new
 * Runnable is added.
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
     * @return
     */
    boolean isIdle();
}
