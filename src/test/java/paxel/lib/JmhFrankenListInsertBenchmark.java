package paxel.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Warmup(time = 1,iterations = 1)
@Measurement(time = 1,iterations = 1)
@Fork(1)
public class JmhFrankenListInsertBenchmark {

    @Benchmark
    public void addTo______100_k_FrankenList_default(FrankenDataProvider100k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo______100_k_FrankenList_75(FrankenDataProvider100k_75 prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }


    @Benchmark
    public void addTo______100_k_ArrayList(ArrayListDataProvider100k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }
  

    @Benchmark
    public void addTo____1_000_k_FrankenList(FrankenDataProvider1000k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }


    @Benchmark
    public void addTo____1_000_k_ArrayList(ArrayListDataProvider1000k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo___10_000_k_FrankenList(FrankenDataProvider10m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }



    @Benchmark
    public void addTo___10_000_k_ArrayList(ArrayListDataProvider10m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo__100_000_k_FrankenList(FrankenDataProvider100m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo__100_000_k_FrankenList_7500(FrankenDataProvider100m_75 prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }


    @Benchmark
    public void addTo__100_000_k_ArrayList(ArrayListDataProvider100m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    private void addNewValuesToList(final List<Long> unsortedNewValues, final List<Long> listUnderTest) {
        for (Long long1 : unsortedNewValues) {
            int binarySearch = Collections.binarySearch(listUnderTest, long1);
            if (binarySearch < 0) {
                listUnderTest.add((binarySearch * -1) - 1, long1);
            } else {
                listUnderTest.set(binarySearch, long1);
            }
        }
    }

    @State(Scope.Benchmark)
    public static class FrankenDataProvider100k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100000;
            sortedList = new FrankenList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }
    @State(Scope.Benchmark)
    public static class FrankenDataProvider100k_75 {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100000;
            sortedList = new FrankenList<>(75);
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }


    @State(Scope.Benchmark)
    public static class ArrayListDataProvider100k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100000;
            sortedList = new ArrayList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }


    @State(Scope.Benchmark)
    public static class FrankenDataProvider1000k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 1_000_000;
            sortedList = new FrankenList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }

    @State(Scope.Benchmark)
    public static class ArrayListDataProvider1000k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 1_000_000;
            sortedList = new ArrayList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }

    @State(Scope.Benchmark)
    public static class FrankenDataProvider {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 1000000;
            sortedList = new FrankenList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }



    @State(Scope.Benchmark)
    public static class ArrayListDataProvider {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 1000000;
            sortedList = new ArrayList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }

    @State(Scope.Benchmark)
    public static class FrankenDataProvider10m {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 10_000_000;
            sortedList = new FrankenList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }



    @State(Scope.Benchmark)
    public static class ArrayListDataProvider10m {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 10_000_000;
            sortedList = new ArrayList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }


    @State(Scope.Benchmark)
    public static class FrankenDataProvider100m {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100_000_000;
            sortedList = new FrankenList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }


    @State(Scope.Benchmark)
    public static class FrankenDataProvider100m_75 {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100_000_000;
            sortedList = new FrankenList<>(7500);
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }


    @State(Scope.Benchmark)
    public static class ArrayListDataProvider100m {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 100_000_000;
            sortedList = new ArrayList<>();
            unsortedNewValues = new ArrayList<>();
            long current = 0;
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                current += r.nextInt(10);
                sortedList.add(current);
            }

            for (int i = 0; i < 10; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhFrankenListInsertBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
