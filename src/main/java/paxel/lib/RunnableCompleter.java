package paxel.lib;

import java.util.concurrent.CompletableFuture;

/**
 * A Runnable that will complete the given future on completion of the given
 * runnable.
 */
public class RunnableCompleter implements Runnable {

    private final CompletableFuture<Void> future;
    private final Runnable runnable;

    /**
     * Creates an instance of Runnable that combines the given Runnable with the
     * given future.
     *
     * @param runnable The wrapped runnable.
     * @param future The future that will be notified of the result.
     */
    public RunnableCompleter(Runnable runnable, CompletableFuture<Void> future) {
        this.runnable = runnable;
        this.future = future;
    }

    /**
     * This runs the wrapped runnable and updates the future.
     */
    @Override
    public void run() {
        try {
            runnable.run();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }
}
