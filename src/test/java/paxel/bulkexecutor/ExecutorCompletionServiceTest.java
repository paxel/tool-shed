package paxel.bulkexecutor;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExecutorCompletionServiceTest {

    @Test
    public void testSuccess() {
        ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));

        // the future provides the result "Test" of a Callable
        completionService.submit(() -> "Test").thenAccept(s -> assertThat(s, is("Test")));

        // the future provides null of a successful Runnable
        completionService.submit(new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        }).thenAccept(s -> assertThat(s, is(IsNull.nullValue())));

        // the future provides the result given after a successful runnable run
        completionService.submit(new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        }, "Test").thenAccept(s -> assertThat(s, is("Test")));
    }

    @Test
    public void testFailRunable() {
        ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));
        // the Runnable throws an exception, that is verified and converted.
        completionService.submit(() -> {
            throw new IllegalArgumentException("Bumm");
        }).exceptionally(e -> {
            assertThat(e.getMessage(), is("Bumm"));
            return "Bumm";
        });

    }

    @Test
    public void testFailRunableWithResult() {
        ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));
        // the Runnable throws an exception, that is verified and converted.
        completionService.submit(() -> {
            throw new IllegalArgumentException("Bumm");
        }, "Success").exceptionally(e -> {
            assertThat(e.getMessage(), is("Bumm"));
            return "Bumm";
        });

    }

    @Test
    public void testFailCallable() {
        ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));
        // the Callable throws an exception, that is verified and converted.
        completionService.submit((Callable<String>) () -> {
            throw new IllegalArgumentException("Bumm");
        }).exceptionally(e -> {
            assertThat(e.getMessage(), is("Bumm"));
            return "Bumm";
        });
    }
}
