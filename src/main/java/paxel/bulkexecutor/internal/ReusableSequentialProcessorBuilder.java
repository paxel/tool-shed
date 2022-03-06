package paxel.bulkexecutor.internal;

import paxel.bulkexecutor.ErrorHandler;
import paxel.bulkexecutor.SequentialProcessor;
import paxel.bulkexecutor.SequentialProcessorBuilder;
import paxel.bulkexecutor.internal.MultiSourceSequentialProcessor;
import paxel.bulkexecutor.internal.SingleSourceSequentialProcessor;

import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

public class ReusableSequentialProcessorBuilder implements SequentialProcessorBuilder {
    private ExecutorService executorService;
    private boolean multiSource = true;
    private int limit = 0;
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
    public SequentialProcessor build() {
        if (multiSource) {
            if (limit > 0) {
                return new MultiSourceSequentialProcessor(executorService, batchSize, errorHandler, limit);
            } else {
                return new MultiSourceSequentialProcessor(executorService, batchSize, errorHandler);
            }
        } else if (limit > 0) {
            return new SingleSourceSequentialProcessor(executorService, batchSize, errorHandler, limit);
        } else {
            return new SingleSourceSequentialProcessor(executorService, batchSize, errorHandler);
        }
    }
}
