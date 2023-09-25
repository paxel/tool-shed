package paxel.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Warmup(time = 1,iterations = 1,batchSize = 1,timeUnit = TimeUnit.SECONDS)
public class JmhFrankenListInsertBenchmark {

    @Benchmark
    public void addTo125k_Entries_FrankenList(FrankenDataProvider125k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo125k_Entries_LinkedList(LinkedListDataProvider125k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo125k_Entries_ArrayList(ArrayListDataProvider125k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo250k_Entries_FrankenList(FrankenDataProvider250k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo250k_Entries_LinkedList(LinkedListDataProvider250k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo250k_Entries_ArrayList(ArrayListDataProvider250k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo500k_Entries_FrankenList(FrankenDataProvider500k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo500k_Entries_LinkedList(LinkedListDataProvider500k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo500k_Entries_ArrayList(ArrayListDataProvider500k prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo1000k_Entries_FrankenList(FrankenDataProvider prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo1000k_Entries_LinkedList(LinkedListDataProvider prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo1000k_Entries_ArrayList(ArrayListDataProvider prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo10_000k_Entries_FrankenList(FrankenDataProvider10m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo10_000k_Entries_LinkedList(LinkedListDataProvider10m prov, Blackhole bh) {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addTo10_000k_Entries_ArrayList(ArrayListDataProvider10m prov, Blackhole bh) {
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
    public static class FrankenDataProvider125k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 125000;
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
    public static class LinkedListDataProvider125k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 125000;
            sortedList = new LinkedList<>();
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
    public static class ArrayListDataProvider125k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 125000;
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
    public static class FrankenDataProvider250k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 250000;
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
    public static class LinkedListDataProvider250k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 250000;
            sortedList = new LinkedList<>();
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
    public static class ArrayListDataProvider250k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 250000;
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
    public static class FrankenDataProvider500k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 500000;
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
    public static class LinkedListDataProvider500k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 500000;
            sortedList = new LinkedList<>();
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
    public static class ArrayListDataProvider500k {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 500000;
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
    public static class LinkedListDataProvider {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 1000000;
            sortedList = new LinkedList<>();
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
    public static class LinkedListDataProvider10m {

        List<Long> sortedList;
        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 10_000_000;
            sortedList = new LinkedList<>();
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

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhFrankenListInsertBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
