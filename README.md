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
        processor.add(() -> bigJimmy.accept(this::currentX()));
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
        processor.add(() -> bigJimmy.complete(this::currentX()));
        return bigJimmy;
    }


...

     // ask for value and go that much back
     bunny.askX().thenApply(r->bunny.sniffle(r));


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
