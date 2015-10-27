
package org.junit.core.util;

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

	public static boolean isEmpty(CharSequence charSequence) {
		return (charSequence == null || charSequence.length() == 0);
	}

	public static boolean nullSafeEquals(Object obj1, Object obj2) {
		return (obj1 == null ? obj2 == null : obj1.equals(obj2));
	}

	public static String nullSafeToString(Class<?>... classes) {
		if (classes == null || classes.length == 0) {
			return "";
		}
		return stream(classes).map(Class::getName).collect(joining(", "));
	}

}
