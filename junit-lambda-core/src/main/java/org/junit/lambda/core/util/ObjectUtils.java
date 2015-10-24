
package org.junit.lambda.core.util;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class ObjectUtils {

	private ObjectUtils() {
		/* no-op */
	}

	public static boolean nullSafeEquals(Object expected, Object actual) {
		return (expected == null ? actual == null : expected.equals(actual));
	}

	public static String nullSafeToString(Class<?>... classes) {
		if (classes == null || classes.length == 0) {
			return "";
		}
		return stream(classes).map(Class::getName).collect(joining(", "));
	}

}
