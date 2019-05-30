package paxel.bulkexecutor;

import java.util.concurrent.CompletableFuture;

/**
 * A Runnable that will complete the given future on completion.
 */
public class FutureRunnable implements Runnable {

    private final CompletableFuture<Void> future;
    private final Runnable runnable;

    /**
     * Creates an instance of Runnable that combines the given Runnable with the
     * given future. The given future will be executed if this is instance is
     * run.
     *
     * @param runnable The wrapped runnable.
     * @param future The future that will be notified of the result.
     */
    public FutureRunnable(Runnable runnable, CompletableFuture<Void> future) {
        this.runnable = runnable;
        this.future = future;
    }

    public void run() {
        try {
            runnable.run();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }
}
