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
import static org.junit.jupiter.api.Fail.fail;

import java.util.function.Supplier;

public class AssertNull {

	public static void assertNull(Object actual) {
		assertNull(actual, () -> null);
	}

	public static void assertNull(Object actual, String message) {
		assertNull(actual, () -> message);
	}

	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		if (actual != null) {
			failNotNull(actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failNotNull(Object actual, String message) {
		fail(buildPrefix(message) + "expected: <null> but was: <" + actual + ">", null, actual);
	}
}
