package paxel.lib;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Rust-like Result
 *
 * @param <V> the value type of the Result.
 * @param <E> the error type of the Result.
 */

public record Result<V, E>(V value, E error, ResultStatus status) {

    /**
     * Creates a successful Result with a value.
     *
     * @param value The successful value.
     * @return The new Result.
     */
    public static <U, X> Result<U, X> ok(U value) {
        return new Result<>(value, null, ResultStatus.SUCCESS);
    }

    /**
     * Creates a failed result with an error.
     *
     * @param error The error value.
     * @return The new Result.
     */
    public static <U, X> Result<U, X> err(X error) {
        return new Result<>(null, error, ResultStatus.FAIL);
    }

    /**
     * Executes a {@link Callable} and creates a Result from the return value.
     * In case the Callable throws an {@link Exception}, the Result will contain it as error.
     *
     * @param callable the process to be executed.
     * @return the result of the process.
     */
    public static <U, X extends Exception> Result<U, X> from(Callable<U> callable) {
        try {
            return new Result<>(callable.call(), null, ResultStatus.SUCCESS);
        } catch (Exception e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }

    /**
     * Queries a {@link Supplier} and creates a Result from the return value.
     * In case the Supplier throws an {@link Exception}, the Result will contain it as error.
     *
     * @param supplier the Supplier to be queried.
     * @return the result of the process.
     */
    public static <U, X extends RuntimeException> Result<U, X> from(Supplier<U> supplier) {
        try {
            return new Result<>(supplier.get(), null, ResultStatus.SUCCESS);
        } catch (RuntimeException e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }

    /**
     * Queries an {@link UnstableExecutable} and creates a Result from the given fallback value.
     * In case the executable throws an {@link Exception}, the Result will contain it as error.
     *
     * @param executable the Executable to be queried.
     * @param fallback   The Result value
     * @return the result of the process.
     */
    public static <U, X extends Exception> Result<U, X> fromExecutableOrElse(UnstableExecutable executable, U fallback) {
        try {
            executable.execute();
            return new Result<>(fallback, null, ResultStatus.SUCCESS);
        } catch (Exception e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }

    /**
     * Queries an {@link UnstableExecutable} and creates a Result from the given fallback supplier.
     * In case the executable throws an {@link Exception}, the Result will contain it as error.
     *
     * @param executable       the Executable to be queried.
     * @param fallbackSupplier The Result value provider
     * @return the result of the process.
     */
    public static <U, X extends Exception> Result<U, X> fromExecutableOrElse(UnstableExecutable executable, Supplier<U> fallbackSupplier) {
        try {
            executable.execute();
            return new Result<>(fallbackSupplier.get(), null, ResultStatus.SUCCESS);
        } catch (Exception e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }

    /**
     * Queries an {@link UnstableExecutable} and creates a Void Result.
     * In case the executable throws an {@link Exception}, the Result will contain it as error.
     *
     * @param executable the Executable to be queried.
     * @return the result of the process.
     */
    public static <X extends Exception> Result<Void, X> fromExecutable(UnstableExecutable executable) {
        try {
            executable.execute();
            return new Result<>(null, null, ResultStatus.SUCCESS);
        } catch (Exception e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }

    /**
     * Queries an {@link Runnable} and creates a Void Result.
     * In case the runnable throws a {@link RuntimeException}, the Result will contain it as error.
     *
     * @param runnable the Runnable to be queried.
     * @return the result of the process.
     */
    public static <X extends RuntimeException> Result<Void, X> from(Runnable runnable) {
        try {
            runnable.run();
            return new Result<>(null, null, ResultStatus.SUCCESS);
        } catch (RuntimeException e) {
            return new Result<>(null, (X) e, ResultStatus.FAIL);
        }
    }




    public boolean isSuccess() {
        return status.isSuccess();
    }

    public boolean hasFailed() {
        return !status.isSuccess();
    }

    /**
     * Retrieves the Result if successful. Otherwise, a ResultException is thrown.
     *
     * @return the Value
     */
    @Override
    public V value() {
        return switch (status) {
            case SUCCESS -> value;
            default -> throw new ResultException(String.format("No value in Result. Error was %s", verboseError()));
        };
    }

    /**
     * Retrieves the Result if successful. Otherwise, the fallback is returned.
     *
     * @return the Value.
     */
    public V getValueOr(V fallBack) {
        return switch (status) {
            case SUCCESS -> value;
            default -> fallBack;
        };
    }

    /**
     * Retrieves the Result if successful. Otherwise, the result of the Supplier is returned.
     *
     * @return the Value.
     */
    public V getValueOrGet(Supplier<V> fallBack) {
        return switch (status) {
            case SUCCESS -> value;
            default -> fallBack.get();
        };
    }

    /**
     * Returns a new Result.
     * The given function is called with value and error. Both might be null.
     * The result of the given function is returned as new Result.
     *
     * @return the Value.
     */
    public <U, X> Result<U, X> map(BiFunction<V, E, Result<U, X>> mapper) {
        return mapper.apply(value, error);
    }

    /**
     * Returns a new Result.
     * If this result is successful, the new Result will contain the value provided by the valueMapper Function.
     * If this result has failed, the new Result will contain the value provided by the errorMapper Function.
     *
     * @return the Value.
     */
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
                    throw new ResultException(String.format("Can't map the value of a failed Result. Error was %s", verboseError()));
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
                    throw new ResultException(String.format("Can't map the error of a successful Result. Value was %s", verboseValue()));
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
