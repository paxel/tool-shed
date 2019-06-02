package paxel.bulkexecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is a simple CompletionService that combines {@link Callable}s and
 * {@link Runnable}s with CompleteableFutures. after submitting, the resulting
 * future can be used to chain actions depending on the execution result. If the
 * chained actions are executed by the caller or the executer is completely
 * depending on when the execution finishes.
 */
public class ExecutorCompletionService {

    private final ExecutorService ex;

    public ExecutorCompletionService(ExecutorService ex) {
        this.ex = ex;
    }

    public void shutdown() {
        ex.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return ex.shutdownNow();
    }

    public boolean isShutdown() {
        return ex.isShutdown();
    }

    public boolean isTerminated() {
        return ex.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return ex.awaitTermination(timeout, unit);
    }

    public <T> CompletableFuture<T> submit(Callable<T> task) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ex.submit(new FutureCallable(task, completableFuture));
        return completableFuture;
    }

    public <T> CompletableFuture<T> submit(Runnable task, T result) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        ex.submit(new FutureRunnable(task, completableFuture));
        // if successfull, change the result
        return completableFuture.thenApply(f -> result);
    }

    public CompletableFuture<Void> submit(Runnable task) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        ex.submit(new FutureRunnable(task, completableFuture));
        // if successfull, change the result
        return completableFuture;
    }
}
