# mvn dependency

To use this small lib you can simply add it via maven

```xml
<dependency>
    <groupId>io.github.paxel</groupId>
    <artifactId>group-executor</artifactId>
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
Q: I can use Virtual Threads now
A: Damn. you're right. This thing is outdated!

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

Q: well that's not very comfortable.   
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
For each ArrayList a meta-object stores the global start index of the LinkedList.

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
The main use-case what the FrankenList was designed for was being used with a ListIterator
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

# Overall Benchmark results
```
Benchmark                                                                  (entries)   Mode  Cnt       Score       Error  Units
p.bulkexecutor.JmhActorBasedSumTest.runBatch                                     N/A  thrpt    5       2.112 ±     0.023  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runOneThreadSingleSourceActors               N/A  thrpt    5       2.099 ±     0.023  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runThreeThreadsMultiSourceActors             N/A  thrpt    5       2.271 ±     0.016  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runThreeThreadsSingleSourceActors            N/A  thrpt    5       2.267 ±     0.019  ops/s
p.bulkexecutor.JmhActorBasedSumTest.runTwoThreadsSingleSourceActors              N/A  thrpt    5       2.262 ±     0.016  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor                 10  thrpt   25  664963.186 ±  2642.725  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor              10000  thrpt   25    4882.673 ±    27.570  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredFor           10000000  thrpt   25       1.267 ±     0.033  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet              10  thrpt   25  657429.079 ±  1330.734  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet           10000  thrpt   25    4752.512 ±    31.196  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredForGet        10000000  thrpt   25       1.222 ±     0.024  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream              10  thrpt   25  647523.544 ± 12258.140  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream           10000  thrpt   25    5183.430 ±   147.312  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFilteredStream        10000000  thrpt   25       1.285 ±     0.020  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                         10  thrpt   25  587503.214 ±  2129.120  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                      10000  thrpt   25    1363.292 ±     8.358  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectFor                   10000000  thrpt   25       0.316 ±     0.019  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                      10  thrpt   25  583630.222 ± 13366.557  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                   10000  thrpt   25    1342.964 ±     4.951  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectForGet                10000000  thrpt   25       0.303 ±     0.026  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                      10  thrpt   25  581644.066 ±  7920.941  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                   10000  thrpt   25    2688.518 ±    37.447  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.collectStream                10000000  thrpt   25       0.432 ±     0.038  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                        10  thrpt   25  736151.479 ±  3155.612  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                     10000  thrpt   25   36880.698 ±   130.982  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskFor                  10000000  thrpt   25      40.940 ±     0.321  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                     10  thrpt   25  739221.619 ±  1912.345  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet                  10000  thrpt   25   41050.551 ±    91.541  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskForGet               10000000  thrpt   25      41.129 ±     0.408  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                     10  thrpt   25  723869.606 ±  2431.528  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream                  10000  thrpt   25   40064.198 ±    59.569  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.easyTaskStream               10000000  thrpt   25      40.649 ±     0.385  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                       10  thrpt   25  722470.351 ±  3136.001  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                    10000  thrpt   25   14321.763 ±    54.372  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskFor                 10000000  thrpt   25      15.281 ±     0.049  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                    10  thrpt   25  726179.363 ±  1329.968  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet                 10000  thrpt   25   14316.874 ±    37.767  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskForGet              10000000  thrpt   25      15.048 ±     0.031  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                    10  thrpt   25  710265.478 ±  4781.733  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream                 10000  thrpt   25   14117.353 ±    83.238  ops/s
p.bulkexecutor.JmhStreamPerformanceMeasurement.heavyTaskStream              10000000  thrpt   25      14.642 ±     0.038  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_125k_Entries            N/A  thrpt   25    9180.892 ±    20.040  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_250k_Entries            N/A  thrpt   25    5944.973 ±    29.631  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_a_500k_Entries            N/A  thrpt   25     561.069 ±    21.827  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_b_1m_Entries              N/A  thrpt   25     368.572 ±    27.282  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToArrayListWith_c_10m_Entries             N/A  thrpt   25      11.290 ±     1.563  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_125k_Entries          N/A  thrpt   25    4676.405 ±    24.237  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_250k_Entries          N/A  thrpt   25    4445.311 ±    11.150  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_a_500k_Entries          N/A  thrpt   25    3325.115 ±     9.251  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_b_1m_Entries            N/A  thrpt   25    2840.254 ±    39.343  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToFrankenListWith_c_10m_Entries           N/A  thrpt   25     479.949 ±    21.255  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_125k_Entries           N/A  thrpt   25     141.131 ±     0.601  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_250k_Entries           N/A  thrpt   25      59.416 ±     0.661  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_a_500k_Entries           N/A  thrpt   25      28.828 ±     0.336  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_b_1m_Entries             N/A  thrpt   25      13.417 ±     0.144  ops/s
p.lib.JmhFrankenListInsertBenchmark.addToLinkedListWith_c_10m_Entries            N/A  thrpt   25       0.605 ±     0.025  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_125k_Entries          N/A  thrpt   25   28237.782 ±   127.130  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_250k_Entries          N/A  thrpt   25    2663.535 ±     5.597  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_a_500k_Entries          N/A  thrpt   25     420.878 ±    15.698  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_b_1m_Entries            N/A  thrpt   25     360.822 ±    28.025  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToArrayListWith_c_10m_Entries           N/A  thrpt   25      14.015 ±     2.744  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_125k_Entries        N/A  thrpt   25   76074.948 ±   373.469  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_250k_Entries        N/A  thrpt   25   50755.875 ±   235.302  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_a_500k_Entries        N/A  thrpt   25   24579.686 ±   174.601  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_b_1m_Entries          N/A  thrpt   25   13664.496 ±    81.021  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToFrankenListWith_c_10m_Entries         N/A  thrpt   25     737.245 ±     9.600  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_125k_Entries         N/A  thrpt   25   13431.016 ±    36.679  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_250k_Entries         N/A  thrpt   25    1697.580 ±     7.551  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_a_500k_Entries         N/A  thrpt   25    1347.942 ±    11.165  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_b_1m_Entries           N/A  thrpt   25     492.104 ±     2.465  ops/s
p.lib.JmhFrankenListIteratorBenchmark.addToLinkedListWith_c_10m_Entries          N/A  thrpt   25      33.561 ±     2.832  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_a_500k_Entries               N/A  thrpt   25       4.317 ±     0.030  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_b_1m_Entries                 N/A  thrpt   25       1.890 ±     0.015  ops/s
p.lib.JmhFrankenListSortBenchmark.sortArrayListWith_c_10m_Entries                N/A  thrpt   25       0.123 ±     0.002  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_a_500k_Entries             N/A  thrpt   25       2.087 ±     0.006  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_b_1m_Entries               N/A  thrpt   25       0.918 ±     0.007  ops/s
p.lib.JmhFrankenListSortBenchmark.sortFrankenListWith_c_10m_Entries              N/A  thrpt   25       0.072 ±     0.001  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_a_500k_Entries              N/A  thrpt   25       4.026 ±     0.018  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_b_1m_Entries                N/A  thrpt   25       1.594 ±     0.034  ops/s
p.lib.JmhFrankenListSortBenchmark.sortLinkedListWith_c_10m_Entries               N/A  thrpt   25       0.100 ±     0.005  ops/s```
