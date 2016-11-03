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

import static org.junit.jupiter.api.AssertionsUtil.buildPrefix;
import static org.junit.jupiter.api.AssertionsUtil.nullSafeGet;

import java.util.function.Supplier;

public class AssertNotNull {

	public static void assertNotNull(Object actual) {
		assertNotNull(actual, () -> null);
	}

	public static void assertNotNull(Object actual, String message) {
		assertNotNull(actual, () -> message);
	}

	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		if (actual == null) {
			failNull(nullSafeGet(messageSupplier));
		}
	}

	private static void failNull(String message) {
		Assertions.fail(buildPrefix(message) + "expected: not <null>");
	}
}
