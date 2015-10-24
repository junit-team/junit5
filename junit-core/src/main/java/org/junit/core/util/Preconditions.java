
package org.junit.core.util;

import java.util.function.Supplier;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class Preconditions {

	private Preconditions() {
		/* no-op */
	}

	public static void notNull(Object object, String message) {
		condition(object != null, message);
	}

	public static void notNull(Object object, Supplier<String> messageSupplier) {
		condition(object != null, messageSupplier);
	}

	public static void condition(boolean predicate, String message) {
		if (!predicate) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void condition(boolean predicate, Supplier<String> messageSupplier) {
		if (!predicate) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
	}

}
