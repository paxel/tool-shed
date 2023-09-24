package paxel.bulkexecutor;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class JmhStreamPerformanceMeasurement {


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhStreamPerformanceMeasurement.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void collectFor(Blackhole bh, DataProvider data) {
        Set<Long> collect = new HashSet<>();
        for (Long unsortedNewValue : data.unsortedNewValues)
            collect.add(unsortedNewValue);
        bh.consume(collect);
    }

    @Benchmark
    public void collectForGet(Blackhole bh, DataProvider data) {
        Set<Long> collect = new HashSet<>();
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            collect.add(data.unsortedNewValues.get(i));
        bh.consume(collect);
    }

    @Benchmark
    public void collectStream(Blackhole bh, DataProvider data) {
        Set<Long> collect = new HashSet<>(data.unsortedNewValues);
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredFor(Blackhole bh, DataProvider data) {
        Set<Long> collect = new HashSet<>();
        for (Long unsortedNewValue : data.unsortedNewValues) {
            if (unsortedNewValue % 5 == 0)
                collect.add(unsortedNewValue);
        }
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredForGet(Blackhole bh, DataProvider data) {
        Set<Long> collect = new HashSet<>();
        for (int i = 0; i < data.unsortedNewValues.size(); i++) {
            Long aLong = data.unsortedNewValues.get(i);
            if (aLong % 5 == 0)
                collect.add(aLong);
        }
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredStream(Blackhole bh, DataProvider data) {
        Set<Long> collect = data.unsortedNewValues.stream()
                .filter(f -> f % 5 == 0)
                .collect(Collectors.toSet());
        bh.consume(collect);
    }

    @Benchmark
    public void easyTaskFor(Blackhole bh, DataProvider data) {
        for (Long unsortedNewValue : data.unsortedNewValues)
            data.easyTask(unsortedNewValue);
        bh.consume(data.sum);
    }

    @Benchmark
    public void easyTaskForGet(Blackhole bh, DataProvider data) {
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            data.easyTask(data.unsortedNewValues.get(i));
        bh.consume(data.sum);
    }


    @Benchmark
    public void easyTaskStream(Blackhole bh, DataProvider data) {
        data.unsortedNewValues.stream().mapToLong(Long::longValue).forEach(data::easyTask);
        bh.consume(data.sum);
    }

    @Benchmark
    public void heavyTaskFor(Blackhole bh, DataProvider data) {
        for (Long unsortedNewValue : data.unsortedNewValues)
            data.heavyTask(unsortedNewValue);
        bh.consume(data.sum);
    }

    @Benchmark
    public void heavyTaskForGet(Blackhole bh, DataProvider data) {
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            data.heavyTask(data.unsortedNewValues.get(i));
        bh.consume(data.sum);
    }


    @Benchmark
    public void heavyTaskStream(Blackhole bh, DataProvider data) {
        data.unsortedNewValues.stream().mapToLong(Long::longValue).forEach(data::heavyTask);
        bh.consume(data.sum);
    }


    @State(Scope.Benchmark)
    public static class DataProvider {
        @Param({"10", "10000", "10000000"})
        int entries;

        final Random r = new Random(100);

        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            unsortedNewValues = new ArrayList<>();
            for (int i = 0; i < entries; i++) {
                unsortedNewValues.add((long) r.nextInt(entries));
            }
        }

        private long sum;

        public long next() {
            return r.nextLong();
        }

        private void easyTask(long unsortedNewValue) {
            sum = sum + unsortedNewValue;
        }

        private void heavyTask(long unsortedNewValue) {
            if (sum % 2 == 0) {
                sum = unsortedNewValue % 7 * sum % 9;
            } else {
                sum = (long) (sum - Math.pow(unsortedNewValue % 7, 26));
            }
            if (sum < 0) {
                sum = next();
            }
            if (sum > Integer.MAX_VALUE) {
                sum = (long) Math.sinh(next());
            }
        }

    }
}
