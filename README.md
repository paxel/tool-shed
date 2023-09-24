# mvn dependency

To use this small lib you can simply add it via maven

NOTE: This version is not compatible with JAVA 8~~~~

```xml
<dependency>
    <groupId>io.github.paxel</groupId>
    <artifactId>group-executor-java11</artifactId>
    <version><!-- See release page --></version>
</dependency>
```

# Feature group-executor
This is an executor that runs processes that belong to a group sequentially.
Multiple groups run in parallel, depending on how the executor is configured.

A simple example would be to see each group as an entity in a game:
Let's assume we have a game field full of bunnies. Each bunny represents a group, or an actor. It has a state (position, hunger, sex, age, color).
Each action of each bunny is represented by a Runnable that change the status of the bunny. (eat, move, have sex, die, fight)
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

In Rust there are no exceptions, but panics.
A generic set of enums Ok and Err are used to return valid results and errors.
The Ok enum value contains the result and the Err contains the reason why there is no Ok result.
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

The FrankenList combines Array and LinkedList to improve sort and search performance for very full lists.

If you need to maintain data sorted, you usually use either an Array or a LinkedList depending on your use case.

ArrayList has random access, and you can find the index very fast thanks to binary search.
LinkedList can insert and remove objects in the list very fast.

ArrayList has to copy all the elements behind an insert/remove.
LinkedList needs to iterate over all elements to reach an index.

So with growing number of entries Array and LinkedList become unusable.

The FrankenList has a single ArrayList, that contains LinkedLists.
Each LinkedList has a maximum size.
If a LinkedList reaches that size, it is split in half and the lower half inserted into the arraylist.
If a LinkedList is empty, it is removed from the ArrayList.
For each ArrayList a metaobject stores the global start index of the LinkedList.

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
p.bulkexecutor.JmhActorBasedSumTest.runBatch                                     N/A  thrpt    5       1.009 ±    0.007  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runOneThreadSingleSourceActors               N/A  thrpt    5       1.003 ±    0.009  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runThreeThreadsSingleSourceActors            N/A  thrpt    5       1.810 ±    0.009  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runTwoThreadsSingleSourceActors              N/A  thrpt    5       1.808 ±    0.030  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor                 10  thrpt   25  656473.346 ± 2539.758  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor              10000  thrpt   25    4761.892 ±   41.883  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor           10000000  thrpt   25       1.280 ±    0.038  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet              10  thrpt   25  652660.028 ± 2344.051  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet           10000  thrpt   25    4696.996 ±   43.071  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet        10000000  thrpt   25       1.220 ±    0.043  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream              10  thrpt   25  615715.842 ± 2432.973  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream           10000  thrpt   25    5027.059 ±   62.505  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream        10000000  thrpt   25       1.264 ±    0.036  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                         10  thrpt   25  586674.581 ± 2562.389  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                      10000  thrpt   25    1365.258 ±   11.491  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                   10000000  thrpt   25       0.317 ±    0.009  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                      10  thrpt   25  585120.969 ± 2722.496  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                   10000  thrpt   25    1344.990 ±    5.318  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                10000000  thrpt   25       0.321 ±    0.008  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                      10  thrpt   25  566071.217 ± 3468.827  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                   10000  thrpt   25    1349.957 ±   19.733  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                10000000  thrpt   25       0.326 ±    0.006  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                        10  thrpt   25  744888.529 ±  589.249  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                     10000  thrpt   25   39300.740 ±   69.133  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                  10000000  thrpt   25      40.193 ±    0.334  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                     10  thrpt   25  747063.559 ± 1706.281  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                  10000  thrpt   25   42382.579 ±   93.797  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet               10000000  thrpt   25      40.607 ±    0.314  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                     10  thrpt   25  723618.424 ± 4771.630  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                  10000  thrpt   25   40415.690 ±  180.454  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream               10000000  thrpt   25      40.329 ±    0.295  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                       10  thrpt   25  705737.175 ±  714.481  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                    10000  thrpt   25    9322.546 ±   25.508  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                 10000000  thrpt   25      15.376 ±    0.104  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                    10  thrpt   25  701908.881 ±  918.762  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                 10000  thrpt   25   14032.623 ±    9.661  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet              10000000  thrpt   25      14.745 ±    0.091  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                    10  thrpt   25  696836.642 ±  811.225  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                 10000  thrpt   25   13400.804 ±   13.761  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream              10000000  thrpt   25      14.716 ±    0.098  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_125k_Entries            N/A  thrpt   25    9099.352 ±   11.940  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_250k_Entries            N/A  thrpt   25    4233.407 ±  157.011  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_500k_Entries            N/A  thrpt   25    1200.570 ±   39.041  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_b_1m_Entries              N/A  thrpt   25     699.431 ±   19.313  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_c_10m_Entries             N/A  thrpt   25      17.041 ±    1.878  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_125k_Entries          N/A  thrpt   25    4949.389 ±   46.911  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_250k_Entries          N/A  thrpt   25    4629.170 ±   47.673  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_500k_Entries          N/A  thrpt   25    3525.308 ±    7.400  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_b_1m_Entries            N/A  thrpt   25    3003.960 ±   14.893  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_c_10m_Entries           N/A  thrpt   25     408.070 ±   62.804  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_125k_Entries           N/A  thrpt   25     139.650 ±    0.835  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_250k_Entries           N/A  thrpt   25      59.734 ±    0.507  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_500k_Entries           N/A  thrpt   25      28.826 ±    0.476  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_b_1m_Entries             N/A  thrpt   25      13.846 ±    0.161  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_c_10m_Entries            N/A  thrpt   25       0.637 ±    0.031  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_125k_Entries          N/A  thrpt   25   27027.683 ±   76.806  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_250k_Entries          N/A  thrpt   25    2176.698 ±   60.193  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_500k_Entries          N/A  thrpt   25     793.033 ±   14.621  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_b_1m_Entries            N/A  thrpt   25     537.646 ±   12.530  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_c_10m_Entries           N/A  thrpt   25      19.878 ±    1.484  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_125k_Entries        N/A  thrpt   25   74947.718 ±  438.849  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_250k_Entries        N/A  thrpt   25   47483.056 ±  287.671  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_500k_Entries        N/A  thrpt   25   23779.868 ±  136.446  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_b_1m_Entries          N/A  thrpt   25   13020.950 ±   70.893  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_c_10m_Entries         N/A  thrpt   25     717.162 ±   15.896  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_125k_Entries         N/A  thrpt   25   13518.301 ±   42.791  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_250k_Entries         N/A  thrpt   25    1731.051 ±    9.098  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_500k_Entries         N/A  thrpt   25    1371.940 ±    6.425  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_b_1m_Entries           N/A  thrpt   25     497.723 ±    3.428  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_c_10m_Entries          N/A  thrpt   25      32.712 ±    3.883  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_a_500k_Entries               N/A  thrpt   25       4.424 ±    0.037  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_b_1m_Entries                 N/A  thrpt   25       1.903 ±    0.014  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_c_10m_Entries                N/A  thrpt   25       0.125 ±    0.001  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_a_500k_Entries             N/A  thrpt   25       1.978 ±    0.013  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_b_1m_Entries               N/A  thrpt   25       0.916 ±    0.008  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_c_10m_Entries              N/A  thrpt   25       0.072 ±    0.001  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_a_500k_Entries              N/A  thrpt   25       3.758 ±    0.054  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_b_1m_Entries                N/A  thrpt   25       1.632 ±    0.024  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_c_10m_Entries               N/A  thrpt   25       0.102 ±    0.004  ops/s
```
