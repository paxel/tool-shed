package paxel.bulkexecutor.internal;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MultiSourceSequentialProcessorTest {

    private int value;
    private List<Integer> values;

    @Before
    public void init() {
        value = 0;
        values = new ArrayList<>();
    }

    private MultiSourceSequentialProcessor p;

    public MultiSourceSequentialProcessorTest() {
    }

    @Test
    public void testExecuteOne() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 0, false);

        p.add(() -> this.value = 1);

        waitForProcessingFinish(exe);

        assertThat(value, is(1));

    }

    @Test
    public void testExecuteOneBlocking() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 1, true);

        p.add(() -> this.value = 1);

        waitForProcessingFinish(exe);

        assertThat(value, is(1));

    }

    @Test
    public void testExecute1000() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 0, false);

        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
        }
        waitForProcessingFinish(exe);

        assertThat(values, is(result));

    }

    @Test
    public void testExecute1000Blocking() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 1, true);

        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
        }
        waitForProcessingFinish(exe);

        assertThat(values, is(result));

    }

    @Test
    public void testExecute100Interrupted() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 0, false);

        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
            Thread.sleep(1);
        }
        waitForProcessingFinish(exe);

        assertThat(values, is(result));
    }

    @Test
    public void testExecute100InterruptedBlocking() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new MultiSourceSequentialProcessor(exe, 1, a -> true, 1, true);

        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
            Thread.sleep(1);
        }
        waitForProcessingFinish(exe);

        assertThat(values, is(result));
    }

    private void waitForProcessingFinish(final ExecutorService exe) throws InterruptedException {
        while (p.size() > 0) {
            Thread.sleep(10);
        }

        exe.shutdown();

        exe.awaitTermination(1, TimeUnit.DAYS);
    }
}
