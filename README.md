# group-executor
This is an executor that runs processes that belong to a group sequentially.
Multiple groups run in parallel, depending on how the executor is configured.

The main purpose is to handle messages to actors by managing the messages globally instead of giving each actor an input queue and a thread.

A simple example would be to see each group as an entity in a game.

Let's assume we have a game field full with bunnies. Each bunny represents a group, or an actor. It has a state (position, hunger, sex, age, color).
Each action of each bunny is represented by Runnables that change the status of the bunny. (eat, move, have sex, die, fight)
Each bunny can only do one action at once, but all bunnies act at the same time.

This executor does exactly that. Depending on the underlying ExecutorService, either there are exactly as many Threads as groups exist, or everything is processed with a limited amount of Threads.

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


Another feature this package provides is the ExecutorCompletionService, that converts the lame Futures of the Executor framework to the mighty CompletionFutures.

```java
ExecutorCompletionService completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(1));

// the future provides the result "Test" of a Callable
completionService.submit(() -> doSomething())
  // if the process was successful, also do this:
  .thenAccept(s -> System.out.println("Result was "+s))));
```
