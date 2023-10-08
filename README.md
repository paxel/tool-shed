# mvn dependency

To use this small lib you can simply add it via maven

```xml
<dependency>
    <groupId>io.github.paxel</groupId>
    <artifactId>tool-shed</artifactId>
    <version><!-- See release page --></version>
</dependency>
```

# Feature group-executor

This component got completely obsolete with Virtual Threads

# Feature Result<V,E>

In Rust-lang there are no exceptions, but there are panics.
A generic set of enums Ok and Err are used to return valid results and errors.
The Ok enum value contains the result, and the Err contains the reason why there is no Ok result.
There is some code candy to handle the two types that don't exist in Java.

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
The benchmark adds 10 random numbers into the sorted list of the given number of elements.
The Frankenlist has a bucket size of 750 (except if marked differently) 

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

# Overall Benchmark results
```
JmhFrankenListInsertBenchmark.addTo__100_000_k_ArrayList            thrpt           0.238          ops/s
JmhFrankenListInsertBenchmark.addTo__100_000_k_FrankenList          thrpt          15.352          ops/s
JmhFrankenListInsertBenchmark.addTo__100_000_k_FrankenList_7500     thrpt          38.935          ops/s

JmhFrankenListInsertBenchmark.addTo___10_000_k_ArrayList            thrpt          18.533          ops/s
JmhFrankenListInsertBenchmark.addTo___10_000_k_FrankenList          thrpt         486.693          ops/s

JmhFrankenListInsertBenchmark.addTo____1_000_k_ArrayList            thrpt         844.837          ops/s
JmhFrankenListInsertBenchmark.addTo____1_000_k_FrankenList          thrpt        2746.037          ops/s

JmhFrankenListInsertBenchmark.addTo______100_k_ArrayList            thrpt       13066.907          ops/s
JmhFrankenListInsertBenchmark.addTo______100_k_FrankenList_75       thrpt       15244.226          ops/s
JmhFrankenListInsertBenchmark.addTo______100_k_FrankenList_default  thrpt        7210.971          ops/s
```

The benchmark shows, that for very full lists (100 million entries) the **Frankenlist** in default settings is ~70 times faster than an ArrayList.
If the bucket size is increased from 750 to 7500, the **Frankenlist** is ~180 times faster.

For less filled lists (100.000 entries) The **Arraylist** is two times faster than the Frankenlist with default settings.
If the buckt size is decreased from 750 to 75, the **Frankenlist** is slightly faster than the Arraylist.


