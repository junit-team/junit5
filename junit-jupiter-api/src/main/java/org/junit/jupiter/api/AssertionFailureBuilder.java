/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

public class AssertionFailureBuilder {

	private Object message;
	private Throwable cause;
	private final List<Throwable> suppressed = new ArrayList<>();
	private boolean mismatch;
	private Object expected;
	private Object actual;
	private String reason;
	private boolean includeValuesInMessage = true;

	public static AssertionFailureBuilder assertionFailure() {
		return new AssertionFailureBuilder();
	}

	private AssertionFailureBuilder() {
	}

	public AssertionFailureBuilder message(Object message) {
		this.message = message;
		return this;
	}

	public AssertionFailureBuilder reason(String reason) {
		this.reason = reason;
		return this;
	}

	public AssertionFailureBuilder cause(Throwable cause) {
		this.cause = cause;
		return this;
	}

	public AssertionFailureBuilder suppressed(Throwable suppressed) {
		this.suppressed.add(suppressed);
		return this;
	}

	public AssertionFailureBuilder suppressed(Throwable... suppressed) {
		this.suppressed.addAll(Arrays.asList(suppressed));
		return this;
	}

	public AssertionFailureBuilder expected(Object expected) {
		this.mismatch = true;
		this.expected = expected;
		return this;
	}

	public AssertionFailureBuilder actual(Object actual) {
		this.mismatch = true;
		this.actual = actual;
		return this;
	}

	public AssertionFailureBuilder includeValuesInMessage(boolean includeValuesInMessage) {
		this.includeValuesInMessage = includeValuesInMessage;
		return this;
	}

	public void buildAndThrow() throws AssertionFailedError {
		throw build();
	}

	public AssertionFailedError build() {
		String reason = nullSafeGet(this.reason);
		if (mismatch && includeValuesInMessage) {
			reason = (reason == null ? "" : reason + ", ") + formatValues(expected, actual);
		}
		String message = nullSafeGet(this.message);
		if (reason != null) {
			message = buildPrefix(message) + reason;
		}
		AssertionFailedError assertionFailedError = mismatch //
				? new AssertionFailedError(message, expected, actual, cause) //
				: new AssertionFailedError(message, cause);
		suppressed.forEach(assertionFailedError::addSuppressed);
		return assertionFailedError;
	}

	private static String nullSafeGet(Object messageOrSupplier) {
		if (messageOrSupplier == null) {
			return null;
		}
		if (messageOrSupplier instanceof Supplier) {
			Object message = ((Supplier<?>) messageOrSupplier).get();
			return message == null ? null : message.toString();
		}
		return String.valueOf(messageOrSupplier);
	}

	private static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}

	private static String formatValues(Object expected, Object actual) {
		String expectedString = toString(expected);
		String actualString = toString(actual);
		if (expectedString.equals(actualString)) {
			return String.format("expected: %s but was: %s", formatClassAndValue(expected, expectedString),
				formatClassAndValue(actual, actualString));
		}
		return String.format("expected: <%s> but was: <%s>", expectedString, actualString);
	}

	private static String formatClassAndValue(Object value, String valueString) {
		// If the value is null, return <null> instead of null<null>.
		if (value == null) {
			return "<null>";
		}
		String classAndHash = getClassName(value) + toHash(value);
		// if it's a class, there's no need to repeat the class name contained in the valueString.
		return (value instanceof Class ? "<" + classAndHash + ">" : classAndHash + "<" + valueString + ">");
	}

	private static String toString(Object obj) {
		if (obj instanceof Class) {
			return getCanonicalName((Class<?>) obj);
		}
		return StringUtils.nullSafeToString(obj);
	}

	private static String toHash(Object obj) {
		return (obj == null ? "" : "@" + Integer.toHexString(System.identityHashCode(obj)));
	}

	private static String getClassName(Object obj) {
		return (obj == null ? "null"
				: obj instanceof Class ? getCanonicalName((Class<?>) obj) : obj.getClass().getName());
	}
}
