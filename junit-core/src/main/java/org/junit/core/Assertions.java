
package org.junit.core;

import org.junit.core.util.ObjectUtils;

public final class Assertions {

	private Assertions() {
	}

	public static void fail(String message) {
		if (message == null) {
			throw new AssertionError();
		}
		throw new AssertionError(message);
	}

	public static void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}

	public static void assertTrue(String message, boolean condition) {
		if (!condition) {
			fail(message);
		}
	}

	public static void assertFalse(boolean condition) {
		assertFalse(null, condition);
	}

	public static void assertFalse(String message, boolean condition) {
		if (condition) {
			fail(message);
		}
	}

	public static void assertEqual(Object expected, Object actual) {
		assertEqual(null, expected, actual);
	}

	public static void assertEqual(String message, Object expected, Object actual) {
		if (!ObjectUtils.nullSafeEquals(expected, actual)) {
			failNotEqual(message, expected, actual);
		}
	}

	public static void assertNotEqual(Object unexpected, Object actual) {
		assertNotEqual(null, unexpected, actual);
	}

	public static void assertNotEqual(String message, Object unexpected, Object actual) {
		if (ObjectUtils.nullSafeEquals(unexpected, actual)) {
			failEqual(message, actual);
		}
	}

	public static void assertEqual(long expected, long actual) {
		assertEqual(null, expected, actual);
	}

	public static void assertEqual(String message, long expected, long actual) {
		if (expected != actual) {
			failNotEqual(message, Long.valueOf(expected), Long.valueOf(actual));
		}
	}

	public static void assertNull(Object object) {
		assertNull(null, object);
	}

	public static void assertNull(String message, Object object) {
		if (object != null) {
			failNotNull(message, object);
		}
	}

	public static void assertNotNull(Object object) {
		assertNotNull(null, object);
	}

	public static void assertNotNull(String message, Object object) {
		if (object == null) {
			failNull(message);
		}
	}

	public static void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}

	public static void assertSame(String message, Object expected, Object actual) {
		if (expected != actual) {
			failNotSame(message, expected, actual);
		}
	}

	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(null, unexpected, actual);
	}

	public static void assertNotSame(String message, Object unexpected, Object actual) {
		if (unexpected == actual) {
			failSame(message);
		}
	}

	public static void assertThrows(Class<? extends Throwable> expected, Executable executable) {
		expectThrows(expected, executable);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T expectThrows(Class<T> expected, Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable actual) {
			if (expected.isInstance(actual)) {
				return (T) actual;
			}
			else {
				String message = Assertions.format("unexpected exception type thrown;", expected.getSimpleName(),
					actual.getClass().getSimpleName());
				throw new AssertionError(message, actual);
			}
		}
		String message = String.format("expected %s to be thrown, but nothing was thrown", expected.getSimpleName());
		throw new AssertionError(message);
	}

	private static void failEqual(String message, Object actual) {
		String formatted = "Values should be different. ";
		if (message != null) {
			formatted = message + ". ";
		}

		formatted += "Actual: " + actual;
		fail(formatted);
	}

	private static void failNull(String message) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected not null");
	}

	private static void failNotNull(String message, Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected null, but was:<" + actual + ">");
	}

	private static void failSame(String message) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected not same");
	}

	private static void failNotSame(String message, Object expected, Object actual) {
		String formatted = "";
		if (message != null) {
			formatted = message + " ";
		}
		fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
	}

	private static void failNotEqual(String message, Object expected, Object actual) {
		fail(format(message, expected, actual));
	}

	private static String format(String message, Object expected, Object actual) {
		String formatted = "";
		if (message != null && !message.equals("")) {
			formatted = message + " ";
		}
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString)) {
			return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
					+ formatClassAndValue(actual, actualString);
		}
		else {
			return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
		}
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String className = (value == null ? "null" : value.getClass().getName());
		return className + "<" + valueString + ">";
	}

}
