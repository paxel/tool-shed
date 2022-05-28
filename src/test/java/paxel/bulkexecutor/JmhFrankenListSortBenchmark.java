package paxel.bulkexecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhFrankenListSortBenchmark {
    
    @Benchmark
    public void sortFrankenListWith_a_500k_Entries(FrankenDataProvider500k prov, Blackhole bh) throws InterruptedException {
        Collections.sort(prov.unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }
    
    @Benchmark
    public void sortLinkedListWith_a_500k_Entries(LinkedListDataProvider500k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }
    
    @Benchmark
    public void sortArrayListWith_a_500k_Entries(ArrayListDataProvider500k prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    // @Benchmark
    public void sortFrankenListWith_b_1m_Entries(FrankenDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    // @Benchmark
    public void sortLinkedListWith_b_1m_Entries(LinkedListDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    //  @Benchmark
    public void sortArrayListWith_b_1m_Entries(ArrayListDataProvider prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    //  @Benchmark
    public void sortFrankenListWith_c_10m_Entries(FrankenDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    //  @Benchmark
    public void sortLinkedListWith_c_10m_Entries(LinkedListDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }

    //  @Benchmark
    public void sortArrayListWith_c_10m_Entries(ArrayListDataProvider10m prov, Blackhole bh) throws InterruptedException {
        final List<Long> unsortedNewValues = prov.unsortedNewValues;
        Collections.sort(unsortedNewValues);
        bh.consume(prov.unsortedNewValues);
    }
    
    @State(Scope.Benchmark)
    public static class FrankenDataProvider500k {
        
        List<Long> unsortedNewValues;
        
        @Setup(Level.Invocation)
        public void init() {
            final int max = 500_000;
            unsortedNewValues = new FrankenList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            final int max = 500_000;
            unsortedNewValues = new LinkedList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            final int max = 500_000;
            unsortedNewValues = new ArrayList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new FrankenList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new LinkedList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new ArrayList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new FrankenList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new LinkedList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
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
            unsortedNewValues = new ArrayList<>();
            Random r = new Random(100);
            for (int i = 0; i < max; i++) {
                unsortedNewValues.add((long) r.nextInt(max));
            }
        }
        
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JmhFrankenListSortBenchmark.class.getSimpleName())
                .build();
        
        new Runner(opt).run();
    }
    
}
