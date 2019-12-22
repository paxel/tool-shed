# group-executor
This is an executor that runs processes that belong to a group sequentially.
Multiple groups run in parallel, depending on how the executor is configured.

The main purpose is to handle messages to actors by managing the messages globally instead of giving each actor an input queue and a thread.




