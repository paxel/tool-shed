package paxel.lib;

/**
 * This can be used as a Runnable that can fail or a Callable that doesn't provide a value.
 */
public interface UnstableExecutable {
    void execute();
}
