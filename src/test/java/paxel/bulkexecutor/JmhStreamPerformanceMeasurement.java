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
    public void collectFor(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = new HashSet<>();
        for (Long unsortedNewValue : data.unsortedNewValues)
            collect.add(unsortedNewValue);
        bh.consume(collect);
    }

    @Benchmark
    public void collectForGet(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = new HashSet<>();
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            collect.add(data.unsortedNewValues.get(i));
        bh.consume(collect);
    }

    @Benchmark
    public void collectStream(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = data.unsortedNewValues.stream().collect(Collectors.toSet());
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredFor(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = new HashSet<>();
        for (Long unsortedNewValue : data.unsortedNewValues) {
            if (unsortedNewValue % 5 == 0)
                collect.add(unsortedNewValue);
        }
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredForGet(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = new HashSet<>();
        for (int i = 0; i < data.unsortedNewValues.size(); i++) {
            Long aLong = data.unsortedNewValues.get(i);
            if (aLong % 5 == 0)
                collect.add(aLong);
        }
        bh.consume(collect);
    }

    @Benchmark
    public void collectFilteredStream(Blackhole bh, DataProvider10m data) {
        Set<Long> collect = data.unsortedNewValues.stream()
                .filter(f -> f % 5 == 0)
                .collect(Collectors.toSet());
        bh.consume(collect);
    }

    @Benchmark
    public void easyTaskFor(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        for (Long unsortedNewValue : data.unsortedNewValues)
            data.easyTask(unsortedNewValue);
        bh.consume(data.sum);
    }

    @Benchmark
    public void easyTaskForGet(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            data.easyTask(data.unsortedNewValues.get(i));
        bh.consume(data.sum);
    }


    @Benchmark
    public void easyTaskStream(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        data.unsortedNewValues.stream().mapToLong(Long::longValue).forEach(f -> data.easyTask(f));
        bh.consume(data.sum);
    }

    @Benchmark
    public void heavyTaskFor(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        for (Long unsortedNewValue : data.unsortedNewValues)
            data.heavyTask(unsortedNewValue);
        bh.consume(data.sum);
    }

    @Benchmark
    public void heavyTaskForGet(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        for (int i = 0; i < data.unsortedNewValues.size(); i++)
            data.heavyTask(data.unsortedNewValues.get(i));
        bh.consume(data.sum);
    }


    @Benchmark
    public void heavyTaskStream(Blackhole bh, DataProvider10m data) {
        data.sum = 0;
        data.unsortedNewValues.stream().mapToLong(Long::longValue).forEach(f -> data.heavyTask(f));
        bh.consume(data.sum);
    }


    @State(Scope.Benchmark)
    public static class DataProvider10m {
        Random r = new Random(100);

        List<Long> unsortedNewValues;

        @Setup(Level.Invocation)
        public void init() {
            final int max = 10_000_000;
            unsortedNewValues = new ArrayList<>();
            for (int i = 0; i < max; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
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
