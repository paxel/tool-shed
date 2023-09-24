package paxel.bulkexecutor;

import lombok.Getter;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// only fork 1 JVM per benchmark
@SuppressWarnings("ResultOfMethodCallIgnored")
@Fork(1)
// Five times 2 second warmup per benchmark
@Warmup(iterations = 5, time = 2)
// Five times 2 second measurement per benchmark
@Measurement(iterations = 5, time = 2)
// in micros
@OutputTimeUnit(TimeUnit.SECONDS)
public class JmhActorBasedSumTest {

    @Benchmark
    public void runOneThreadSingleSourceActors(DataProvider1 prov) throws InterruptedException {
        testIt(prov.a1, prov.a2, prov.a3, prov.latch, prov.exe);
    }

    @Benchmark
    public void runTwoThreadsSingleSourceActors(DataProvider2 prov) throws InterruptedException {
        testIt(prov.a1, prov.a2, prov.a3, prov.latch, prov.exe);
    }

    @Benchmark
    public void runThreeThreadsSingleSourceActors(DataProvider3 prov) throws InterruptedException {
        testIt(prov.a1, prov.a2, prov.a3, prov.latch, prov.exe);
    }

    @Benchmark
    public void runThreeThreadsMultiSourceActors(DataProvider3_2 prov) throws InterruptedException {
        testIt(prov.a1, prov.a2, prov.a3, prov.latch, prov.exe);
    }

    @Benchmark
    public void runBatch(DataProviderBatch prov) throws InterruptedException {
        testIt(prov.a1, prov.a2, prov.a3, prov.latch, prov.exe);
    }

    private void addAllValues(Actor3 a3, Actor2 a2) {
        for (int i = 0; i < 100; i++) {
            a3.tell(23);
            a2.tell(12);
            a2.tell(23);
            a2.tell(12);
            a2.tell(23);
            a3.tell(12);
            a3.tell(22);
            a3.tell(42);
            a3.tell(62);
        }
    }

    private void testIt(Actor1 a1, Actor2 a2, Actor3 a3, CountDownLatch latch, ExecutorService exe)
            throws InterruptedException {
        addAllValues(a3, a2);
        final ShutDown shutDown = new ShutDown();
        a3.tell(shutDown);
        a2.tell(shutDown);
        latch.await();
        if (a1.getResult() != 11316) {
            throw new IllegalStateException("Expected 11316 but got " + a1.getResult());
        }
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.DAYS);
    }

    @State(Scope.Benchmark)
    public static class DataProvider2 {

        private ExecutorService exe;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(2);
            GroupingExecutor g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.create().build(), latch);
            a2 = new Actor2(g.create().build(), a1);
            a3 = new Actor3(g.create().build(), a1);
        }

    }

    @State(Scope.Benchmark)
    public static class DataProvider3 {

        private ExecutorService exe;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(3);
            GroupingExecutor g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.create().build(), latch);
            a2 = new Actor2(g.create().build(), a1);
            a3 = new Actor3(g.create().build(), a1);
        }

    }

    @State(Scope.Benchmark)
    public static class DataProvider3_2 {

        private ExecutorService exe;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(3);
            GroupingExecutor g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.create().build(), latch);
            a2 = new Actor2(g.create().build(), a1);
            a3 = new Actor3(g.create().build(), a1);
        }

    }

    @State(Scope.Benchmark)
    public static class DataProvider1 {

        private ExecutorService exe;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(1);
            GroupingExecutor g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.create().build(), latch);
            a2 = new Actor2(g.create().build(), a1);
            a3 = new Actor3(g.create().build(), a1);
        }

    }

    @State(Scope.Benchmark)
    public static class DataProviderBatch {

        private ExecutorService exe;
        private Actor1 a1;
        private Actor2 a2;
        private Actor3 a3;
        private CountDownLatch latch;

        @Setup(Level.Invocation)
        public void init() {
            exe = Executors.newFixedThreadPool(1);
            GroupingExecutor g = new GroupingExecutor(exe);
            latch = new CountDownLatch(2);
            a1 = new Actor1(g.create().setBatchSize(100).build(), latch);
            a2 = new Actor2(g.create().setBatchSize(100).build(), a1);
            a3 = new Actor3(g.create().setBatchSize(100).build(), a1);
        }

    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public static class Actor1 {

        @Getter
        private long result;

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final CountDownLatch latch;

        private Actor1(SequentialProcessor createSingleSourceSequentialProcessor, CountDownLatch latch) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.latch = latch;
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(latch::countDown);
        }

        public void tell(byte[] a) {
            createSingleSourceSequentialProcessor.add(() -> result += a.length);
        }

    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public static class Actor2 {

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final Actor1 a1;
        final Random r = new Random(100);

        private Actor2(SequentialProcessor createSingleSourceSequentialProcessor, Actor1 a1) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.a1 = a1;
        }

        public void tell(Integer a) {
            int size = r.nextInt(a);
            byte[] bytes = new byte[size];
            createSingleSourceSequentialProcessor.add(() -> {
                try {
                    Thread.sleep(1, 300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a1.tell(bytes);
            });
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(() -> a1.tell(a));
        }
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public static class Actor3 {

        private final SequentialProcessor createSingleSourceSequentialProcessor;
        private final Actor1 a1;
        final Random r = new Random(1000);

        private Actor3(SequentialProcessor createSingleSourceSequentialProcessor, Actor1 a1) {
            this.createSingleSourceSequentialProcessor = createSingleSourceSequentialProcessor;
            this.a1 = a1;
        }

        public void tell(Integer a) {
            int size = r.nextInt(a);
            byte[] bytes = new byte[size];
            createSingleSourceSequentialProcessor.add(() -> {
                try {
                    Thread.sleep(0, 40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a1.tell(bytes);
            });
        }

        public void tell(ShutDown a) {
            createSingleSourceSequentialProcessor.add(() -> a1.tell(a));
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
    public void testOneThread() throws InterruptedException {
        DataProvider1 dataProvider = new DataProvider1();
        dataProvider.init();
        this.runOneThreadSingleSourceActors(dataProvider);
    }

    @Test
    public void testTwoThreads() throws InterruptedException {
        DataProvider2 dataProvider = new DataProvider2();
        dataProvider.init();
        this.runTwoThreadsSingleSourceActors(dataProvider);
    }

    @Test
    public void testThreeThreads() throws InterruptedException {
        DataProvider3 dataProvider = new DataProvider3();
        dataProvider.init();
        this.runThreeThreadsSingleSourceActors(dataProvider);
    }

    @Test
    public void testThreeThreadsMulti() throws InterruptedException {
        DataProvider3_2 dataProvider = new DataProvider3_2();
        dataProvider.init();
        this.runThreeThreadsMultiSourceActors(dataProvider);
    }

    @Test
    public void testBatch() throws InterruptedException {
        DataProviderBatch dataProvider = new DataProviderBatch();
        dataProvider.init();
        this.runBatch(dataProvider);
    }
}
