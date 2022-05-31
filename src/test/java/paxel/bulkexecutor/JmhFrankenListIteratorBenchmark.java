package paxel.bulkexecutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhFrankenListIteratorBenchmark {

    @Benchmark
    public void addToFrankenListWith_a_125k_Entries(FrankenDataProvider125k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToLinkedListWith_a_125k_Entries(LinkedListDataProvider125k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToArrayListWith_a_125k_Entries(ArrayListDataProvider125k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToFrankenListWith_a_250k_Entries(FrankenDataProvider250k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToLinkedListWith_a_250k_Entries(LinkedListDataProvider250k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToArrayListWith_a_250k_Entries(ArrayListDataProvider250k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToFrankenListWith_a_500k_Entries(FrankenDataProvider500k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToLinkedListWith_a_500k_Entries(LinkedListDataProvider500k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToArrayListWith_a_500k_Entries(ArrayListDataProvider500k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToFrankenListWith_b_1m_Entries(FrankenDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToLinkedListWith_b_1m_Entries(LinkedListDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToArrayListWith_b_1m_Entries(ArrayListDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToFrankenListWith_c_10m_Entries(FrankenDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToLinkedListWith_c_10m_Entries(LinkedListDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    @Benchmark
    public void addToArrayListWith_c_10m_Entries(ArrayListDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        final List<Long> listUnderTest = prov.sortedList;
        addNewValuesToList(unsortedNewValues, listUnderTest);
        bh.consume(listUnderTest);
    }

    private void addNewValuesToList(final List<Long> unsortedNewValues, final List<Long> listUnderTest) {
        int index = unsortedNewValues.get(0).intValue();
        ListIterator<Long> listIterator = listUnderTest.listIterator(index);
        Iterator<Long> iterator = unsortedNewValues.iterator();
        while (iterator.hasNext()) {
            if (listIterator.hasNext()) {
                listIterator.next();
            } else {
                listIterator.previous();
            }
            listIterator.remove();
            listIterator.add(iterator.next());
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
                .include(JmhFrankenListIteratorBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
