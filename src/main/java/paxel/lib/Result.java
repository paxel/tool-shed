package paxel.lib;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Rust like Result
 *
 * @param <V> the value type of the Result.
 * @param <E> the error type of the Result.
 */
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

    private Result(V value, E error, ResultStatus success) {
        this.value = value;
        this.error = error;
        this.status = success;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status.isSuccess();
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

    public <U, X> Result<U, X> mapValue(Function<V, U> valueMapper) {
        switch (status) {
            case SUCCESS:
                return Result.ok(valueMapper.apply(value));
            case FAIL:
            default:
                throw new IllegalStateException(String.format("Can't map the value of a failed Result. Error was %s", verboseError()));
        }
    }

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

    public E getError() {
        switch (status) {
            case FAIL:
                return error;
            case SUCCESS:
            default:
                throw new ResultException("No error in Result. Value was " + value);
        }
    }

    public enum ResultStatus {
        FAIL, SUCCESS {
            @Override
            boolean isSuccess() {
                return true;
            }
        };

        boolean isSuccess() {
            return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result<?, ?> result = (Result<?, ?>) o;
        return Objects.equals(value, result.value) && Objects.equals(error, result.error) && status == result.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, error, status);
    }
}
