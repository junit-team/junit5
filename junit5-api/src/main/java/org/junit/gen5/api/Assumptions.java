
package org.junit.gen5.api;

import java.util.function.Supplier;

import org.opentestalliance.TestAbortedException;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class Assumptions {

	private Assumptions() {
		/* no-op */
	}

	public static void assumeTrue(boolean condition) {
		if (!condition) {
			throw new TestAbortedException("Assumption failed: condition is not true");
		}
	}

	public static void assumeTrue(boolean condition, String message) {
		if (!condition) {
			throw new TestAbortedException("Assumption failed: " + message);
		}
	}

	public static void assumeTrue(boolean condition, Supplier<String> messageSupplier) {
		if (!condition) {
			throw new TestAbortedException(messageSupplier.get());
		}
	}

	public static void assumeFalse(boolean condition) {
		if (condition) {
			throw new TestAbortedException("Assumption failed: condition is not false");
		}
	}

	public static void assumeFalse(boolean condition, String message) {
		if (condition) {
			throw new TestAbortedException(message);
		}
	}

	public static void assumeFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			throw new TestAbortedException("Assumption failed: " + messageSupplier.get());
		}
	}

	public static void assumingThat(boolean condition, Executable executable) {
		if (condition) {
			try {
				executable.execute();
			}
			catch (AssertionError | RuntimeException e) {
				// rethrow
				throw e;
			}
			catch (Throwable e) {
				// TODO Don't wrap Throwables such as OutOfMemoryError, etc.
				throw new RuntimeException("Wrapped exception thrown from Executable", e);
			}
		}
	}

}
