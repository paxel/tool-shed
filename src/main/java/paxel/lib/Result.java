package paxel.lib;

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

    public Result(V value, E error, ResultStatus status) {
        this.value = value;
        this.error = error;
        this.status = status;
    }

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

    public ResultStatus getStatus() {
        return this.status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Result)) return false;
        final Result<?, ?> other = (Result<?, ?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$value = this.value;
        final Object other$value = other.value;
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        final Object this$error = this.error;
        final Object other$error = other.error;
        if (this$error == null ? other$error != null : !this$error.equals(other$error)) return false;
        final Object this$status = this.status;
        final Object other$status = other.status;
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Result;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $value = this.value;
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        final Object $error = this.error;
        result = result * PRIME + ($error == null ? 43 : $error.hashCode());
        final Object $status = this.status;
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
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
        switch (status) {
            case FAIL:
                return "SUCCESS:" + verboseError();
            case SUCCESS:
            default:
                return "FAILURE:" + verboseValue();
        }
    }


}
