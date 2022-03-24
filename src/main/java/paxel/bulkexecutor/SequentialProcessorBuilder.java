package paxel.bulkexecutor;

import java.util.concurrent.ExecutorService;

/**
 * Represents a builder of {@link SequentialProcessor}.
 */
public interface SequentialProcessorBuilder {

    /**
     * If the ProcessorBuilder receives messages from multiple sources, the input needs to be syncronized.
     * If a builder only receives messages from a single source, set it to false for performance improvement.
     * Otherwise leave it to the default (true).
     *
     * @param multiSource The flag for multi source
     * @return The builder.
     */
    SequentialProcessorBuilder setMultiSource(boolean multiSource);

    /**
     * The queue limit of the ProcessorBuilder. If set to 0 (default) the queue is unbound.
     * The limited queue has a better performance, but can lead to dropped messages.
     *
     * @param limit The message limit.
     * @return The builder.
     */
    SequentialProcessorBuilder setLimited(int limit);

    /**
     * The number of messages that are tried to be processed, before freeing the thread for another processor.
     * If you have lots of fast processes and less threads than sequential processors, it makes sense to increase the batch size.
     * If you have more threads than sequential processors it also makes sense to choose big batch sizes for less overhead.
     * If you have long running processes and less threads than sequential processors, big batch sizes can stall processing of other sequential processors.
     * Default is 1
     *
     * @param batchSize the number of processes finished, before the thread is released and the sequential processors waits for a new thread.
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
     * If this is set to true, the processing queue is blocking and will wait until a place in the queue is available for a new message.
     * This can lead to deadlocks in case processes are communicating with each other.
     * If this is set to true and the limit is 0, the blocking value is ignored.
     * <p>
     * handle with care.
     *
     * @return The builder.
     */
    SequentialProcessorBuilder setBlocking(boolean blocking);

    /**
     * Retrieve the executor service.
     *
     * @return the service.
     */
    ExecutorService getExecutorService();

    /**
     * Retrieve the executor service.
     *
     * @return the service.
     */
    boolean isMultiSource();

    /**
     * Retrieve the executor service.
     *
     * @return the service.
     */
    boolean isBlocking();

    /**
     * Retrieve the queue limit.
     *
     * @return the limit.
     */
    int getLimit();

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
