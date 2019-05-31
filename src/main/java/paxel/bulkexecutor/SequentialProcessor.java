package paxel.bulkexecutor;

/**
 * A Grouped Queue guarantees, that all added Runnables are processed
 * sequential.
 */
public interface SequentialProcessor {

    /**
     * Adds a new Runnable to the Queue. The execution is guaranteed to be one
     * after another.
     *
     * @param r The new runnable
     * @return true if the runnable was accepted.
     */
    boolean add(Runnable r);

    /**
     * The current size of the Queue.
     *
     * @return size.
     */
    int size();
}
