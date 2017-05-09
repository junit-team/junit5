/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageContains;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertionsFailTests {

	@Test
	void failWithString() {
		try {
			fail("test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithMessageSupplier() {
		try {
			fail(() -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithNullString() {
		try {
			fail((String) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
		}
	}

	@Test
	void failWithNullMessageSupplier() {
		try {
			fail((Supplier<String>) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
		}
	}

	@Test
	void failWithStringAndThrowable() {
		try {
			fail("message", new Throwable("cause"));
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "message");
			Throwable cause = ex.getCause();
			assertMessageContains(cause, "cause");
		}
	}

	@Test
	void failWithThrowable() {
		try {
			fail((String) null, new Throwable("cause"));
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
			Throwable cause = ex.getCause();
			assertMessageContains(cause, "cause");
		}
	}

	@Test
	void failWithStringAndNullThrowable() {
		try {
			fail("message", (Throwable) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "message");
			if (ex.getCause() != null) {
				throw new AssertionError("Cause should have been null");
			}
		}
	}

	@Test
	void failWithNullStringAndThrowable() {
		try {
			fail((String) null, new Throwable("cause"));
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
			Throwable cause = ex.getCause();
			assertMessageContains(cause, "cause");
		}
	}

	@Test
	void failUsableAsAnExpression() {
		// @formatter:off
		long count = Collections.emptySet().stream()
				.peek(element -> fail("peek should never be called"))
				.filter(element -> fail("filter should never be called", new Throwable("cause")))
				.map(element -> fail(new Throwable("map should never be called")))
				.sorted((e1, e2) -> fail(() -> "sorted should never be called"))
				.count();
		// @formatter:on
		assertEquals(0L, count);
	}

}
