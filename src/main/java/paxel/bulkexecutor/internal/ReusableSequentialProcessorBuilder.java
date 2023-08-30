package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import paxel.bulkexecutor.SequentialProcessor;
import paxel.bulkexecutor.SequentialProcessorBuilder;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

public class ReusableSequentialProcessorBuilder implements SequentialProcessorBuilder {
    private ExecutorService executorService;
    private int batchSize = 1;
    private ErrorHandler errorHandler = x -> true;

    public ReusableSequentialProcessorBuilder(ExecutorService executorService) {

        this.executorService = executorService;
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
    public ExecutorService getExecutorService() {
        return executorService;
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
        return new ConcurrentDequeSequentialProcessor(executorService, batchSize, errorHandler);
    }
}
