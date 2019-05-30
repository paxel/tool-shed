package paxel.bulkexecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * A Runnable that will complete the given future on completion.
 *
 * @param <V> the result type of the Callable.
 */
public class FutureCallable<V> implements Runnable {

    private final Callable<V> callable;

    private final CompletableFuture<V> future;

    /**
     * Creates an instance of Runnable that combines the given Callable with the
     * given future. The given future will be executed if this is instance is
     * run.
     *
     * @param callable The wrapped callable.
     * @param future The future that will be notified of the result.
     */
    public FutureCallable(Callable<V> callable, CompletableFuture<V> future) {
        this.callable = callable;
        this.future = future;
    }

    /**
     * This runs the wrapped callable and updates the future.
     */
    @Override
    public void run() {
        try {
            V call = callable.call();
            future.complete(call);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }
}
