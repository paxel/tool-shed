package paxel.bulkexecutor;

/**
 * This handler checks Throwables and decides if a {@link SequentialProcessors} should continue
 * processing.
 */
@FunctionalInterface
public interface ErrorHandler {

    /**
     * Checks the Throwable caused by running a Runnable.
     *
     * @param t the Throwable.
     * @return true if the {@link SequentialProcessors} should continue
     * processing. false if the {@link SequentialProcessors} should abort.
     */
    boolean check(Throwable t);
}
