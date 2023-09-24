package paxel.bulkexecutor;

import java.util.concurrent.ExecutorService;

/**
 * Represents a builder of {@link SequentialProcessor}.
 */
public interface SequentialProcessorBuilder {


    /**
     * The number of messages that are tried to be processed, before freeing the thread for another processor.
     * If you have lots of fast processes and fewer threads than sequential processors, it makes sense to increase the batch size.
     * If you have more threads than sequential processors, it also makes sense to choose big batch sizes for less overhead.
     * If you have long-running processes and fewer threads than sequential processors, big batch sizes can stall processing of other sequential processors.
     * Default is 1
     *
     * @param batchSize the number of processes finished, before the thread is released and the sequential processors wait for a new thread.
     * @return The builder.
     */
    SequentialProcessorBuilder setBatchSize(int batchSize);

    /**
     * The error handler is called whenever a process throws an Exception.
     * The error handler can return true to continue processing or false to abort all processes.
     * The default error handler returns true and ignores the given information.
     *
     * @param errorHandler The error handler.
     * @return The builder.
     */
    SequentialProcessorBuilder setErrorHandler(ErrorHandler errorHandler);


    /**
     * Retrieve the executor service.
     *
     * @return the service.
     */
    ExecutorService getExecutorService();


    /**
     * Retrieve the batch size.
     *
     * @return the size.
     */
    int getBatchSize();

    /**
     * Retrieve the error handler.
     *
     * @return the handler.
     */
    ErrorHandler getErrorHandler();

    /**
     * Builds a {@link SequentialProcessor with the given settings}
     *
     * @return The new processor.
     */
    SequentialProcessor build();
}
