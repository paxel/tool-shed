package paxel.lib;

import java.util.concurrent.CompletableFuture;

public class UnstableExecutableCompleter implements Runnable  {

    private final CompletableFuture<Void> future;
    private final UnstableExecutable runnable;

    /**
     * Creates an instance of Runnable that combines the given Runnable with the
     * given future.
     *
     * @param runnable The wrapped runnable.
     * @param future The future that will be notified of the result.
     */
    public UnstableExecutableCompleter(UnstableExecutable runnable, CompletableFuture<Void> future) {
        this.runnable = runnable;
        this.future = future;
    }

    /**
     * This runs the wrapped runnable and updates the future.
     */
    @Override
    public void run() {
        try {
            runnable.execute();
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }
}
