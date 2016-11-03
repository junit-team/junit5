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

import static org.junit.jupiter.api.AssertionsUtil.nullSafeGet;

import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;

public class Fail {
	public static void fail(String message) {
		fail(() -> message);
	}

	public static void fail(Supplier<String> messageSupplier) {
		throw new AssertionFailedError(nullSafeGet(messageSupplier));
	}

	static void fail(String message, Object expected, Object actual) {
		throw new AssertionFailedError(message, expected, actual);
	}
}
