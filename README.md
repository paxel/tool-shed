# mvn dependency

To use this small lib you can simply depend it via maven

```xml
<dependency>
    <groupId>io.github.paxel</groupId>
    <artifactId>group-executor</artifactId>
    <version>0.10.7</version>
</dependency>
```

# Feature group-executor
This is an executor that runs processes that belong to a group sequentially.
Multiple groups run in parallel, depending on how the executor is configured.

A simple example would be to see each group as an entity in a game:
Let's assume we have a game field full with bunnies. Each bunny represents a group, or an actor. It has a state (position, hunger, sex, age, color).
Each action of each bunny is represented by Runnables that change the status of the bunny. (eat, move, have sex, die, fight)
Each bunny can only do one action at once, but all bunnies act at the same time.

This executor does exactly that; Depending on the thread pooling of the used ExecutorService, either all or some bunnies act concurrently, but each bunny will only do one action at a time.

```java
GroupingExecutor e = new GroupingExecutor(executorService);

// create two groups
SequentialProcessor youngBunny = e.createMultiSourceSequentialProcessor()
SequentialProcessor maleBunny = e.createMultiSourceSequentialProcessor()

maleBunny.addRunnable(()->mb.searchFemale())
youngBunny.addRunnable(()->yb.searchFood())
youngBunny.addRunnable(()->yb.eatFood())
maleBunny.addRunnable(()->mb.sniff())
maleBunny.addRunnable(()->mb.searchFood())
youngBunny.addRunnable(()->yb.cleanFur())
maleBunny.addRunnable(()->mb.search(yb))

```
Of course it makes way more sense to have the SequentialProcessor inside of an instance of bunny and delegate all the action of that instance to the processor. If done correctly, the bunny instance becomes completely threadsafe, because the commands are all executed one after another.

```java
public class Bunny {

    private final SequentialProcessor processor;

    // never used by another thread than the SequentialProcessor
    private int x;

    public Bunny(SequentialProcessor processor) {
        this.processor = processor;
    }

    public void hopUp() {
        processor.add(() -> incX(1));
    }

    public void hopDown() {
        processor.add(() -> incX(-1));
    }

    private void incX(int diff) {
        x += diff;
    }

}
```
*FAQ:*

Q: Why use this anyway? I can just use a single Threaded Executor in the Bunny.   
A: Yes, but if you have 1_000_000 Bunnies, you peak at 1 mio Threads. Good bye.

Q: Well I could use a limited ExecutorService and limit it to 20.   
A: Yes, but then one bunny is hopping concurrently up **and** down. Good bye.

Q: I can not get the value x out of the bunny.   
A: wrong. check that:

```java
   public void askX(IntConsumer bigJimmy) {
        processor.add(() -> bigJimmy.accept(currentX()));
    }

    private int currentX(){
        return x;
    }
```

you will receive the value of x that is current at the point of time that the queued element is processed.

Q: well thats not very comfortable.   
A: just use a CompletableFuture instead an IntConsumer and you can react to the value immediately

```java
   public CompletableFuture<Integer> askX() {
        CompletableFuture<Integer> bigJimmy = new CompletableFuture();
        processor.add(() -> bigJimmy.complete(currentX()));
        return bigJimmy;
    }


...

     // ask for value and poop the x
     bunny.askX().thenAccept(r->bunny.poop(r));


```


This is not expected to be the most performant solution. But it should be fairly simple to use.

# Feature Result<V,E>

In Rsut there are no exceptions, but panics.
A generic set of enums Ok and Err are used to return valid results and errors.
The Ok enum value contains the result and the Err comtains the reason why there is no Ok result.
There are some code candy to handle the two types that don't exist in Java.

But there are places where this procedural handling of Results is handy and leads to more understandable code (at least for me), so I started implementing my own Result class that tries to mimic the RUST way.

```java
    public Result<Login, IOException> ensureLogin(){
        Result<Auth, RESTException> login=checkLoginToRestService();
        if(!login.isSuccess())
          return login.mapError(e->new IOException("Could not login to ID Server",e));

        if(!login.getValue().isLoggedIn())
          return Result.err(new IOException("The ID server doesn't accept our login"));

          return login.mapValue(v->new Login(v.getAuthId()));
    }
```

# Feature ExecutorCompletionService
Another feature this library provides is the ExecutorCompletionService, that converts the lame Futures of the Executor framework to the mighty CompletionFutures.

```java
ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));

// The completionservice returns a CompletableFuture that will be completed, when the given Runnable/Callable is finished
completionService.submit(() -> doSomething())
  // if the process was successful, also do this:
  .thenAccept(s -> System.out.println("Result was "+s))));
```


# Feature FrankenList

The Frankenlist combines Array and LinkedList to improve sort and search performance for very full lists.

If you need to maintain data sorted, you usually use either an Array or a LinkedList depending on your use case.

ArrayList has random access and you can find the index very fast thanks to binary search.
LinkedList can insert and remove objects in the list very fast.

ArrayList has to copy all the elements behind an insert/remove.
LinkedList needs to iterate over all elements to reach an index.

So with growing number of entries Array and LinkedList become unusable.

The FrankenList has a single ArrayList, that contains LinkedLists.
Each LinkedList has a maximum size.
If a LinkedList reaches that size, it is split in half and the lower half inserted into the arraylist.
If a LinkedList is empty, it is removed from the ArrayList.
For each ArrayList a meta object stores the global start index of the LinkedList.

The benefit:
FrankenList has nearly random access: 
* Jump into the ArrayList at the estimated position of the index
  * depending on globalStartIndex of that LinkedList navigate up or down the arrayList until the correct LinkedList is found
  * navigate in this limited size LinkedList
* FrankenList has nearly the speed of a LinkedList of adding removing entries:
  * remove from list
    * if empty remove list from ArrayList
    * update globalStartIndex of all LinkedLists behind the current
  * add to list
    * if limit reached split list and insert lower half to ArrayList
    * update globalStartIndex of all LinkedLists behind the current

```
[0: [globalStartIndex  0; [0:{17},1:{183},2:{3983},3:{9000}]] ],
[1: [globalStartIndex  4; [4:{17000},5:{17001},6:{17002},7:{17003}]] ],
[2: [globalStartIndex  8; [8:{18000}]] ],
[3: [globalStartIndex  9; [9:{18117},10:{18127}11:{18217}]] ],
[4: [globalStartIndex 12; [12:..]] ],
[5: [globalStartIndex 16; [16:..]] ],
[6: [globalStartIndex 20; [20:..]] ],
[7: [globalStartIndex 24; [24:..]] ],
[8: [globalStartIndex 30; [30:..]] ],
[9: [globalStartIndex 32; [32:..]] ],
```

## Benchmarks

### Insert multiple values in a list
The benchmark added 10 random numbers into the sorted list of the given number of elements.
The result shows that ArrayList is incredibly fast for amounts of entries at least up to 125k.
But at 250k already FrankenList is faster, and at 500k more than double the speed of an ArrayList.
At a million entries, the FrankenList is 5 times faster than an ArrayList.
At 10 million entries, it's 26 times faster

**Benchmarked code:**
```java
for (Long long1 : unsortedNewValues) { // 10 random values
    int binarySearch = Collections.binarySearch(listUnderTest, long1);
    if (binarySearch < 0) {
        listUnderTest.add((binarySearch * -1) - 1, long1);
    } else {
        listUnderTest.set(binarySearch, long1);
    }
}
```

**Result:**
| size       | Franken   | Linked  | Array        |
|------------|-----------|---------|--------------|
| 125.000    | 4925      | 139     | **8822**     |
| 250.000    | **4567**  | 59      | 3948         |
| 500.000    | **3523**  | 29      | 1205         |
| 1.000.000  | **2988**  | 13      | 682          |
| 10.000.000 | **429**   | 0       | 16           |

The number is operations per second.
The operation is adding 10 different numbers into the list.
The numbers are guaranteed to be greater than the first and less than the last element in the list.


### Sorting an unsorted list
Sorting an unsorted List is not very effective and should be avoided

**Benchmarked code:**
```java
Collections.sort(unsortedNewValues);
```

**Result:**
| size       | Franken       | Linked  | Array    |
|------------|---------------|---------|----------|
| 500.000    |  1            | 3       | **4**    |
| 1.000.000  |  0            | 1       | **1**    |
| 10.000.000 |  0            | 0       | **0**    |

Because the Sort is using TimSort that is running on an array, ArrayList is unbeatable here.
The FrankenList has to copy all the LinkedLists to an Array and afterwards the sorted array back into the FrankenList.


### Searching an entry and use a ListFilter to manipulate the environment
The main usecase what the FrankenList was designed for was being used with a ListIterator
to manipulate some values at a given position. e.g finding an entry, examining the surrounding entries and
eventually remove, replace and/or insert an entry.

**Benchmarked code:**
```java
int index = unsortedNewValues.get(0).intValue();
ListIterator<Long> listIterator = listUnderTest.listIterator(index);
Iterator<Long> iterator = unsortedNewValues.iterator();
while (iterator.hasNext()) {// 10 random values
    if (listIterator.hasNext()) {
        listIterator.next();
    } else {
        listIterator.previous();
    }
    listIterator.remove();
    listIterator.add(iterator.next());
}
```

**Result:**
| size       | Franken    | Linked  | Array |
|------------|------------|---------|-------|
| 125.000    | **74973**  | 13422   | 25952 |
| 250.000    | **47378**  |  1731   |  2128 |
| 500.000    | **23597**  |  1375   |   793 |
| 1.000.000  | **12921**  |   501   |   531 |
| 10.000.000 |   **718**  |    33   |    17 |

Even in situations where the ArrayList was faster before, it is slower in this
scenario.
It must be said, that the ListIterator here is not optimized.
A dedicated FrankenListIterator will improve this value additionally.

## Benchmark results
```
Benchmark                                                                  (entries)   Mode  Cnt       Score      Error  Units
p.bulkexecutor.JmhActorBasedSumTest.runBatch                                     N/A  thrpt    5       1.008 ±    0.010  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runOneThreadSingleSourceActors               N/A  thrpt    5       1.005 ±    0.017  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runThreeThreadsMultiSourceActors             N/A  thrpt    5       1.806 ±    0.015  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runThreeThreadsSingleSourceActors            N/A  thrpt    5       1.810 ±    0.011  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runTwoThreadsSingleSourceActors              N/A  thrpt    5       1.811 ±    0.012  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor                 10  thrpt   25  653882.086 ± 3531.406  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor              10000  thrpt   25    4576.060 ±  243.218  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor           10000000  thrpt   25       1.256 ±    0.043  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet              10  thrpt   25  654350.627 ± 1974.940  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet           10000  thrpt   25    4674.138 ±   37.355  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet        10000000  thrpt   25       1.192 ±    0.039  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream              10  thrpt   25  606742.410 ± 3662.822  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream           10000  thrpt   25    5010.984 ±   60.958  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream        10000000  thrpt   25       1.270 ±    0.042  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                         10  thrpt   25  585863.503 ±  853.717  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                      10000  thrpt   25    1365.654 ±    9.012  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                   10000000  thrpt   25       0.319 ±    0.007  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                      10  thrpt   25  580922.634 ± 3884.973  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                   10000  thrpt   25    1346.079 ±    7.527  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                10000000  thrpt   25       0.319 ±    0.007  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                      10  thrpt   25  565369.486 ± 5230.052  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                   10000  thrpt   25    1350.088 ±   11.689  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                10000000  thrpt   25       0.322 ±    0.008  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                        10  thrpt   25  747560.283 ± 1152.863  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                     10000  thrpt   25   39218.408 ±  113.281  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                  10000000  thrpt   25      40.352 ±    0.414  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                     10  thrpt   25  749590.271 ± 1107.138  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                  10000  thrpt   25   42309.047 ±  133.748  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet               10000000  thrpt   25      40.585 ±    0.311  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                     10  thrpt   25  730144.357 ± 4963.161  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                  10000  thrpt   25   40499.535 ±  119.321  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream               10000000  thrpt   25      40.364 ±    0.319  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                       10  thrpt   25  705587.126 ± 1641.951  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                    10000  thrpt   25    9320.441 ±   25.115  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                 10000000  thrpt   25      15.378 ±    0.085  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                    10  thrpt   25  703061.303 ± 1800.163  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                 10000  thrpt   25   14016.125 ±   13.268  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet              10000000  thrpt   25      14.742 ±    0.079  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                    10  thrpt   25  698384.357 ±  535.556  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                 10000  thrpt   25   13427.368 ±   14.380  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream              10000000  thrpt   25      14.722 ±    0.084  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_125k_Entries            N/A  thrpt   25    9090.235 ±   12.588  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_250k_Entries            N/A  thrpt   25    4123.223 ±  187.373  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_500k_Entries            N/A  thrpt   25    1210.284 ±   42.788  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_b_1m_Entries              N/A  thrpt   25     711.596 ±    7.803  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_c_10m_Entries             N/A  thrpt   25      17.613 ±    1.798  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_125k_Entries          N/A  thrpt   25    4944.066 ±   45.889  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_250k_Entries          N/A  thrpt   25    4607.993 ±   37.476  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_500k_Entries          N/A  thrpt   25    3528.729 ±    6.771  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_b_1m_Entries            N/A  thrpt   25    3007.390 ±   17.578  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_c_10m_Entries           N/A  thrpt   25     425.624 ±   15.623  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_125k_Entries           N/A  thrpt   25     138.993 ±    1.522  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_250k_Entries           N/A  thrpt   25      59.892 ±    0.464  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_500k_Entries           N/A  thrpt   25      29.523 ±    0.346  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_b_1m_Entries             N/A  thrpt   25      13.864 ±    0.268  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_c_10m_Entries            N/A  thrpt   25       0.626 ±    0.035  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_125k_Entries          N/A  thrpt   25   27035.407 ±   41.315  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_250k_Entries          N/A  thrpt   25    2171.887 ±   67.800  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_500k_Entries          N/A  thrpt   25     790.715 ±   12.703  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_b_1m_Entries            N/A  thrpt   25     541.793 ±    6.731  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_c_10m_Entries           N/A  thrpt   25      19.424 ±    1.811  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_125k_Entries        N/A  thrpt   25   75307.258 ±  456.360  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_250k_Entries        N/A  thrpt   25   47366.785 ±  245.393  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_500k_Entries        N/A  thrpt   25   23711.411 ±  110.209  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_b_1m_Entries          N/A  thrpt   25   12908.306 ±   46.790  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_c_10m_Entries         N/A  thrpt   25     723.838 ±    8.082  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_125k_Entries         N/A  thrpt   25   13505.815 ±   32.275  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_250k_Entries         N/A  thrpt   25    1732.112 ±    6.737  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_500k_Entries         N/A  thrpt   25    1377.152 ±    5.126  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_b_1m_Entries           N/A  thrpt   25     499.209 ±    3.552  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_c_10m_Entries          N/A  thrpt   25      32.809 ±    3.960  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_a_500k_Entries               N/A  thrpt   25       4.400 ±    0.049  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_b_1m_Entries                 N/A  thrpt   25       1.886 ±    0.014  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_c_10m_Entries                N/A  thrpt   25       0.126 ±    0.001  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_a_500k_Entries             N/A  thrpt   25       1.974 ±    0.013  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_b_1m_Entries               N/A  thrpt   25       0.911 ±    0.009  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_c_10m_Entries              N/A  thrpt   25       0.072 ±    0.001  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_a_500k_Entries              N/A  thrpt   25       3.751 ±    0.086  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_b_1m_Entries                N/A  thrpt   25       1.621 ±    0.023  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_c_10m_Entries               N/A  thrpt   25       0.102 ±    0.003  ops/s
```
