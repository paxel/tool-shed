package paxel.lib;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Rust like Result
 *
 * @param <V> the value type of the Result.
 * @param <E> the error type of the Result.
 */

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true)
public class Result<V, E> {
    private final V value;
    private final E error;
    private final ResultStatus status;

    public static <V, E> Result<V, E> ok(V value) {
        return new Result<>(value, null, ResultStatus.SUCCESS);
    }

    public static <V, E> Result<V, E> err(E error) {
        return new Result<>(null, error, ResultStatus.FAIL);
    }

    public boolean isSuccess() {
        return status.isSuccess();
    }

    public boolean hasFailed() {
        return !status.isSuccess();
    }

    public V getValue() {
        switch (status) {
            case SUCCESS:
                return value;
            case FAIL:
            default:
                throw new ResultException(String.format("No value in Result. Error was %s", verboseError()));
        }
    }

    public V getValueOr(V fallBack) {
        switch (status) {
            case SUCCESS:
                return value;
            case FAIL:
            default:
                return fallBack;
        }
    }

    public V getValueOrGet(Supplier<V> fallBack) {
        switch (status) {
            case SUCCESS:
                return value;
            case FAIL:
            default:
                return fallBack.get();
        }
    }

    public <U, X> Result<U, X> map(Function<V, U> valueMapper, Function<E, X> errorMapper) {
        switch (status) {
            case SUCCESS:
                return Result.ok(valueMapper.apply(value));
            case FAIL:
            default:
                return Result.err(errorMapper.apply(error));
        }
    }

    public <U, X> Result<U, X> mapValue(Function<V, U> valueMapper) {
        switch (status) {
            case SUCCESS:
                return Result.ok(valueMapper.apply(value));
            case FAIL:
            default:
                throw new IllegalStateException(String.format("Can't map the value of a failed Result. Error was %s", verboseError()));
        }
    }

    public <U, X> Result<U, X> mapResult(Function<Result<V, E>, Result<U, X>> valueMapper) {
        return valueMapper.apply(this);
    }

    public <U, X> Result<U, X> mapValueToError(Function<V, X> valueToErrorMapper) {
        return Result.err(valueToErrorMapper.apply(value));
    }

    /**
     * Creates a new Result with a new Error.
     *
     * @param errorMapper creates a new Error from the existing one.
     * @return the new Status
     */
    public <U, X> Result<U, X> mapError(Function<E, X> errorMapper) {
        switch (status) {
            case FAIL:
                return Result.err(errorMapper.apply(error));
            case SUCCESS:
            default:
                throw new IllegalStateException(String.format("Can't map the error of a successful Result. Value was %s", verboseValue()));
        }
    }


    private String verboseValue() {
        if (value == null)
            return "<null> value";
        return String.format("%s: %s", value.getClass().getSimpleName(), value);
    }

    private String verboseError() {
        if (error == null)
            return "<null> value";
        return String.format("%s: %s", error.getClass().getSimpleName(), error);
    }

    /**
     * Retrieve the Error.
     *
     * @return the Error
     * @throws ResultException if the Result is successful.
     */
    public E getError() {
        switch (status) {
            case FAIL:
                return error;
            case SUCCESS:
            default:
                throw new ResultException("No error in Result. Value was " + value);
        }
    }

    @Getter
    public enum ResultStatus {
        FAIL(false), SUCCESS(true);

        private final boolean success;

        ResultStatus(boolean success) {
            this.success = success;
        }
    }

    @Override
    public String toString() {
        switch (status) {
            case FAIL:
                return "SUCCESS:" + verboseError();
            case SUCCESS:
            default:
                return "FAILURE:" + verboseValue();
        }
    }


}
