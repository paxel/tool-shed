package paxel.lib;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Rust-like Result
 *
 * @param <V> the value type of the Result.
 * @param <E> the error type of the Result.
 */

public record Result<V, E>(V value, E error, ResultStatus status) {

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

    @Override
    public V value() {
        return switch (status) {
            case SUCCESS -> value;
            default -> throw new ResultException(String.format("No value in Result. Error was %s", verboseError()));
        };
    }

    public V getValueOr(V fallBack) {
        return switch (status) {
            case SUCCESS -> value;
            default -> fallBack;
        };
    }

    public V getValueOrGet(Supplier<V> fallBack) {
        return switch (status) {
            case SUCCESS -> value;
            default -> fallBack.get();
        };
    }

    public <U, X> Result<U, X> map(Function<V, U> valueMapper, Function<E, X> errorMapper) {
        return switch (status) {
            case SUCCESS -> Result.ok(valueMapper.apply(value));
            default -> Result.err(errorMapper.apply(error));
        };
    }

    public <U, X> Result<U, X> mapValue(Function<V, U> valueMapper) {
        return switch (status) {
            case SUCCESS -> Result.ok(valueMapper.apply(value));
            default ->
                    throw new IllegalStateException(String.format("Can't map the value of a failed Result. Error was %s", verboseError()));
        };
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
        return switch (status) {
            case FAIL -> Result.err(errorMapper.apply(error));
            default ->
                    throw new IllegalStateException(String.format("Can't map the error of a successful Result. Value was %s", verboseValue()));
        };
    }


    private String verboseValue() {
        return switch (value) {
            case null -> "<null> value";
            default -> String.format("%s: %s", value.getClass().getSimpleName(), value);
        };
    }

    private String verboseError() {
        return switch (error) {
            case null -> "<null> error";
            default -> String.format("%s: %s", error.getClass().getSimpleName(), error);
        };
    }

    /**
     * Retrieve the Error.
     *
     * @return the Error
     * @throws ResultException if the Result is successful.
     */
    @Override
    public E error() {
        return switch (status) {
            case FAIL -> error;
            default -> throw new ResultException("No error in Result. Value was " + value);
        };
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Result<?, ?> result))
            return false;
        if (!Objects.equals(value, result.value))
            return false;
        if (!Objects.equals(error, result.error))
            return false;
        return status == result.status;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + status.hashCode();
        return result;
    }

    public enum ResultStatus {
        FAIL(false), SUCCESS(true);

        private final boolean success;

        ResultStatus(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return this.success;
        }
    }

    @Override
    public String toString() {
        return switch (status) {
            case SUCCESS -> "SUCCESS:" + verboseError();
            default -> "FAILURE:" + verboseValue();
        };
    }


}
