
package org.junit.gen5.commons.util;

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

	/**
	 * @see ObjectUtils#isEmpty(CharSequence)
	 */
	public static void notEmpty(String str, String message) {
		condition(!ObjectUtils.isEmpty(str), message);
	}

	/**
	 * @see ObjectUtils#isEmpty(CharSequence)
	 */
	public static void notEmpty(String str, Supplier<String> messageSupplier) {
		condition(!ObjectUtils.isEmpty(str), messageSupplier);
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
