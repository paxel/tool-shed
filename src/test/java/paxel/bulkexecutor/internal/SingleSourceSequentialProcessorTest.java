package paxel.bulkexecutor.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

public class SingleSourceSequentialProcessorTest {
    
    private int value;
    private List<Integer> values;
    
    @Before
    public void init() {
        value = 0;
        values = new ArrayList<>();
    }
    private SingleSourceSequentialProcessor p;
    
    public SingleSourceSequentialProcessorTest() {
    }
    
    @Test
    public void testExecuteOne() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new SingleSourceSequentialProcessor(exe);
        
        p.add(() -> this.value = 1);
        
        waitForProcessingFinish(exe);
        
        Assert.assertThat(value, is(1));
        
    }
    
    @Test
    public void testExecute1000() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new SingleSourceSequentialProcessor(exe);
        
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
        }
        waitForProcessingFinish(exe);
        
        Assert.assertThat(values, is(result));
        
    }
    
    @Test
    public void testExecute100Interrupted() throws InterruptedException {
        final ExecutorService exe = Executors.newFixedThreadPool(4);
        p = new SingleSourceSequentialProcessor(exe);
        
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            result.add(i);
            final Integer j = i;
            p.add(() -> this.values.add(j));
            Thread.sleep(1);
        }
        waitForProcessingFinish(exe);
        
        Assert.assertThat(values, is(result));
        
    }
    
    private void waitForProcessingFinish(final ExecutorService exe) throws InterruptedException {
        while (p.size() > 0) {
            Thread.sleep(10);
        }
        
        exe.shutdown();
        
        exe.awaitTermination(1, TimeUnit.DAYS);
    }
}
