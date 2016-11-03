/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionsUtil.format;
import static org.junit.jupiter.api.AssertionsUtil.nullSafeGet;
import static org.junit.jupiter.api.Fail.fail;

import java.util.function.Supplier;

public class AssertSame {

	public static void assertSame(Object expected, Object actual) {
		assertSame(expected, actual, () -> null);
	}

	public static void assertSame(Object expected, Object actual, String message) {
		assertSame(expected, actual, () -> message);
	}

	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotSame(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failNotSame(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

}
