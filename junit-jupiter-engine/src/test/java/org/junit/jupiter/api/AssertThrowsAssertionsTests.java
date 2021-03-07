/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertThrowsAssertionsTests extends AbstractThrowsAssertionsTests {
	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return Assertions.assertThrows(expectedType, executable);
	}

	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return Assertions.assertThrows(expectedType, executable, message);
	}

	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {
		return Assertions.assertThrows(expectedType, executable, messageSupplier);
	}
}
