# group-executor
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
A: Yes, but then one bunny is hopping simultaneously up and down. Good bye.

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

you will receive the value of x that it will have logically after processing all hops, that it was asked to do before.

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

# ExecutorCompletionService
Another feature this package provides is the ExecutorCompletionService, that converts the lame Futures of the Executor framework to the mighty CompletionFutures.

```java
ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));

// The completionservice returns a CompletableFuture that will be completed, when the given Runnable/Callable is finished
completionService.submit(() -> doSomething())
  // if the process was successful, also do this:
  .thenAccept(s -> System.out.println("Result was "+s))));
```


To use this small lib you can simply depend it: https://mvnrepository.com/artifact/io.github.paxel/group-executor/0.9.4

# FrankenList

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
FrankenList has nearly the speed of a LinkedList of adding removing entries:
  * remove from list
    * if empty remove list from ArrayList
    * update globalStartIndex of all LinkedLists behind the current
  * add to list
    * if limit reached split list and insert lower half to ArrayList
    * update globalStartIndex of all LinkedLists behind the current

```
ArrayList
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
``

| *10 ops/s  | Franken       | Linked  | Array        |
|------------|---------------|---------|--------------|
| 125.000    | 4925.107      | 139.068 | **8822.953** |
| 250.000    | **4567.353**  | 59.280  | 3948.059     |
| 500.000    | **3523.361**  | 29.254  | 1205.781     |
| 1.000.000  | **2988.935**  | 13.684  | 682.039      |
| 10.000.000 | **429.508**   | 0.626   | 16.656       |

The benchmark added 10 random numbers into the sorted list of the given number of elements.
The result shows that ArrayList is incredibly fast for amounts of entries at least up to 125k.
But at 250k already FrankenList is faster, and at 500k more than double the speed of an ArrayList.
At a million entries, the FrankenList is 5 times faster than an ArrayList.
At 10 million entries, it's 26 times faster

