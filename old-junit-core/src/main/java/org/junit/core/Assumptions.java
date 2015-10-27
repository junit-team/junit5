
package org.junit.core;

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

	public static void assumeFalse(boolean condition) {
		if (condition) {
			throw new TestAbortedException("Assumption failed: condition is not false");
		}
	}

	public static void assumingThat(boolean condition, Executable executable) {
		if (condition) {
			try {
				executable.execute();
			}
			catch (Throwable e) {
				// TODO Don't wrap Throwables such as OutOfMemoryError, etc.
				throw new RuntimeException("Wrapped exception thrown from Executable", e);
			}
		}
	}

}
