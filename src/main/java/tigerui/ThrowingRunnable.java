package tigerui;

/**
 * A Runnable that can throw
 */
@FunctionalInterface
public interface ThrowingRunnable {
	void run() throws Throwable;
}
