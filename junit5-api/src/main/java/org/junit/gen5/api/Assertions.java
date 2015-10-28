package org.junit.gen5.api;

import org.junit.gen5.commons.util.ObjectUtils;
import org.opentestalliance.AssertionFailedError;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * @author JUnit Community
 * @author Sam Brannen
 * @since 5.0
 */
public final class Assertions {

	private Assertions() {
		/* no-op */
	}

	public static void fail(String message) {
		if (message == null) {
			throw new AssertionFailedError();
		}
		throw new AssertionFailedError(message);
	}

	public static void fail(Supplier<String> messageSupplier) {
		fail(nullSafeGet(messageSupplier));
	}

	public static void assertTrue(boolean condition) {
		assertTrue(condition, (String) null);
	}

	public static void assertTrue(boolean condition, String message) {
		if (!condition) {
			fail(message);
		}
	}

	public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		if (!condition) {
			fail(messageSupplier);
		}
	}

	public static void assertTrue(BooleanSupplier booleanSupplier) {
		assertTrue(booleanSupplier.getAsBoolean(), (String) null);
	}

	public static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		if (!booleanSupplier.getAsBoolean()) {
			fail(message);
		}
	}

	public static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (!booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	public static void assertFalse(boolean condition) {
		assertFalse(condition, (String) null);
	}

	public static void assertFalse(boolean condition, String message) {
		if (condition) {
			fail(message);
		}
	}

	public static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			fail(messageSupplier);
		}
	}

	public static void assertFalse(BooleanSupplier booleanSupplier) {
		assertFalse(booleanSupplier.getAsBoolean(), (String) null);
	}

	public static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		if (booleanSupplier.getAsBoolean()) {
			fail(message);
		}
	}

	public static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	public static void assertNull(Object actual) {
		assertNull(actual, (String) null);
	}

	public static void assertNull(Object actual, String message) {
		if (actual != null) {
			failNotNull(actual, message);
		}
	}

	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		if (actual != null) {
			failNotNull(actual, nullSafeGet(messageSupplier));
		}
	}

	public static void assertNotNull(Object actual) {
		assertNotNull(actual, (String) null);
	}

	public static void assertNotNull(Object actual, String message) {
		if (actual == null) {
			failNull(message);
		}
	}

	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		if (actual == null) {
			failNull(nullSafeGet(messageSupplier));
		}
	}

	public static void assertEquals(Object expected, Object actual) {
		assertEquals(expected, actual, (String) null);
	}

	public static void assertEquals(Object expected, Object actual, String message) {
		if (!Objects.equals(expected, actual)) {
			failNotEqual(expected, actual, message);
		}
	}

	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (!Objects.equals(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		if (Objects.equals(unexpected, actual)) {
			failEqual(actual, message);
		}
	}

	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (Objects.equals(unexpected, actual)) {
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	public static void assertSame(Object expected, Object actual) {
		assertSame(expected, actual, (String) null);
	}

	public static void assertSame(Object expected, Object actual, String message) {
		if (expected != actual) {
			failNotSame(expected, actual, message);
		}
	}

	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotSame(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(unexpected, actual, (String) null);
	}

	public static void assertNotSame(Object unexpected, Object actual, String message) {
		if (unexpected == actual) {
			failSame(message);
		}
	}

	public static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failSame(nullSafeGet(messageSupplier));
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
				String message = Assertions.format(expected.getName(), actual.getClass().getName(),
					"unexpected exception type thrown;");
				throw new AssertionFailedError(message, actual);
			}
		}
		throw new AssertionFailedError(
			String.format("expected %s to be thrown, but nothing was thrown", expected.getName()));
	}

	private static void failEqual(Object actual, String message) {
		String prefix = "Values should be different. ";
		if (!ObjectUtils.isEmpty(message)) {
			prefix = message + ". ";
		}
		fail(prefix + "Actual: " + actual);
	}

	private static void failNull(String message) {
		fail(buildPrefix(message) + "expected not null");
	}

	private static void failNotNull(Object actual, String message) {
		fail(buildPrefix(message) + "expected null, but was:<" + actual + ">");
	}

	private static void failSame(String message) {
		fail(buildPrefix(message) + "expected not same");
	}

	private static void failNotSame(Object expected, Object actual, String message) {
		fail(buildPrefix(message) + "expected same:<" + expected + "> was not:<" + actual + ">");
	}

	private static void failNotEqual(Object expected, Object actual, String message) {
		fail(format(expected, actual, message));
	}

	private static String format(Object expected, Object actual, String message) {
		String prefix = buildPrefix(message);
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString)) {
			return prefix + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
					+ formatClassAndValue(actual, actualString);
		}
		else {
			return prefix + "expected:<" + expectedString + "> but was:<" + actualString + ">";
		}
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String className = (value == null ? "null" : value.getClass().getName());
		return className + "<" + valueString + ">";
	}

	private static String buildPrefix(String message) {
		return (!ObjectUtils.isEmpty(message) ? message + " ==> " : "");
	}

	private static String nullSafeGet(Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

}
