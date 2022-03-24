package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import paxel.bulkexecutor.SequentialProcessor;
import paxel.bulkexecutor.SequentialProcessorBuilder;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

public class ReusableSequentialProcessorBuilder implements SequentialProcessorBuilder {
    private ExecutorService executorService;
    private boolean multiSource = true;
    private int limit;
    private boolean blocking;
    private int batchSize = 1;
    private ErrorHandler errorHandler = x -> true;

    public ReusableSequentialProcessorBuilder(ExecutorService executorService) {

        this.executorService = executorService;
    }

    @Override
    public SequentialProcessorBuilder setMultiSource(boolean multiSource) {
        this.multiSource = multiSource;
        return this;
    }

    @Override
    public SequentialProcessorBuilder setLimited(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("Limit must be a positive number or 0 for no limit");
        this.limit = limit;
        return this;
    }

    @Override
    public SequentialProcessorBuilder setBatchSize(int batchSize) {
        if (batchSize <= 0)
            throw new IllegalArgumentException("Batch size must be a positive number");
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public SequentialProcessorBuilder setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = requireNonNull(errorHandler);
        return this;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    @Override
    public SequentialProcessorBuilder setBlocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public boolean isMultiSource() {
        return multiSource;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public SequentialProcessor build() {
        if (multiSource) {
            if (limit > 0) {
                return new MultiSourceSequentialProcessor(executorService, batchSize, errorHandler, limit, blocking);
            }
            return new MultiSourceSequentialProcessor(executorService, batchSize, errorHandler, 0, false);
        }
        if (limit > 0) {
            return new SingleSourceSequentialProcessor(executorService, batchSize, errorHandler, limit, blocking);
        }
        return new SingleSourceSequentialProcessor(executorService, batchSize, errorHandler, 0, false);
    }
}
