package paxel.bulkexecutor;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

// only fork 1 JVM per benchmark
@Fork(1)
// 5 times 2 second warmup per benchmark
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
// 5 times 2 second measurment per benchmark
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
// in micros
@OutputTimeUnit(TimeUnit.SECONDS)
public class JmhActorBasedSumTest {

    @Benchmark
    public void runTwoThreads(DataProvider2 prov) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            prov.a3.tell(23);
            prov.a2.tell(12);
            prov.a2.tell(23);
            prov.a2.tell(12);
            prov.a2.tell(23);
            prov.a3.tell(12);
            prov.a3.tell(22);
            prov.a3.tell(42);
            prov.a3.tell(62);
        }
        final ShutDown shutDown = new ShutDown();
        prov.a3.tell(shutDown);
        prov.a2.tell(shutDown);
        prov.latch.await();
        if (prov.a1.getResult() != 11316) {
            throw new IllegalStateException("Expected 11316 but got " + prov.a1.getResult());
        }
        prov.exe.shutdown();
        prov.exe.awaitTermination(1, TimeUnit.DAYS);
    }

    @Benchmark
    public void runOneThread(DataProvider1 prov) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            prov.a3.tell(23);
            prov.a2.tell(12);
            prov.a2.tell(23);
            prov.a2.tell(12);
            prov.a2.tell(23);
            prov.a3.tell(12);
            prov.a3.tell(22);
            prov.a3.tell(42);
            prov.a3.tell(62);
        }
        final ShutDown shutDown = new ShutDown();
        prov.a3.tell(shutDown);
        prov.a2.tell(shutDown);
        prov.latch.await();
        if (prov.a1.getResult() != 11316) {
            throw new IllegalStateException("Expected 11316 but got " + prov.a1.getResult());
        }
        prov.exe.shutdown();
        prov.exe.awaitTermination(1, TimeUnit.DAYS);
    }

    @State(Scope.Benchmark)
    public static class DataProvider2 {

        private ExecutorService exe;
        private GroupingExecutor g;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(2);
            g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.createMultiSourceSequentialProcessor(), latch);
            a2 = new Actor2(g.createSingleSourceSequentialProcessor(), a1);
            a3 = new Actor3(g.createSingleSourceSequentialProcessor(), a1);
        }

    }

    @State(Scope.Benchmark)
    public static class DataProvider1 {

        private ExecutorService exe;
        private GroupingExecutor g;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(1);
            g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.createMultiSourceSequentialProcessor(), latch);
            a2 = new Actor2(g.createSingleSourceSequentialProcessor(), a1);
            a3 = new Actor3(g.createSingleSourceSequentialProcessor(), a1);
        }

    }

    public static class Actor1 {

        private long result;

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final CountDownLatch latch;

        private Actor1(SequentialProcessor createSingleSourceSequentialProcessor, CountDownLatch latch) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.latch = latch;
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(() -> {
                latch.countDown();
            });
        }

        public void tell(Integer a) {
            createSingleSourceSequentialProcessor.add(() -> {
                result += a;
            });
        }

        public long getResult() {
            return result;
        }

    }

    public static class Actor2 {

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final Actor1 a1;
        Random r = new Random(100);

        private Actor2(SequentialProcessor createSingleSourceSequentialProcessor, Actor1 a1) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.a1 = a1;
        }

        public void tell(Integer a) {
            createSingleSourceSequentialProcessor.add(() -> {

                a1.tell(r.nextInt(a));
            });
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(() -> {
                a1.tell(a);
            });
        }
    }

    public static class Actor3 {

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final Actor1 a1;
        Random r = new Random(1000);

        private Actor3(SequentialProcessor createSingleSourceSequentialProcessor, Actor1 a1) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.a1 = a1;
        }

        public void tell(Integer a) {
            createSingleSourceSequentialProcessor.add(() -> {
                a1.tell(r.nextInt(a));
            });
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(() -> {
                a1.tell(a);
            });
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhActorBasedSumTest.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    private static class ShutDown {

        public ShutDown() {
        }
    }

    @Test
    public void testTwoThreads() throws InterruptedException {
        DataProvider2 dataProvider = new DataProvider2();
        dataProvider.init();
        this.runTwoThreads(dataProvider);
    }

    @Test
    public void testOneThread() throws InterruptedException {
        DataProvider1 dataProvider = new DataProvider1();
        dataProvider.init();
        this.runOneThread(dataProvider);
    }
}
