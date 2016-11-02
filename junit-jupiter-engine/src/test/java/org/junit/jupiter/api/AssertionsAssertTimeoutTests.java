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

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsAssertTimeoutTests {

	private static ThreadLocal<AtomicBoolean> changed = ThreadLocal.withInitial(() -> new AtomicBoolean(false));

	@Test
	void assertTimeoutForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);

		assertTimeout(ofMillis(500), () -> {
			changed.get().set(true);
		});

		assertTrue(changed.get().get(), "should have executed in the same thread");
	}

	@Test
	void assertTimeoutForExecutableThatThrowsAnException() {
		RuntimeException exception = assertThrows(RuntimeException.class, () -> assertTimeout(ofMillis(500), () -> {
			throw new RuntimeException("not this time");
		}));
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutForExecutableThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(500), () -> fail("enigma")));
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100)));
		assertMessageStartsWith(error, "execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100), "Tempus Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100), () -> "Tempus" + " " + "Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);

		assertTimeoutPreemptively(ofMillis(500), () -> {
			changed.get().set(true);
		});

		assertFalse(changed.get().get(), "should have executed in a different thread");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatThrowsAnException() {
		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> assertTimeoutPreemptively(ofMillis(500), () -> {
				throw new RuntimeException("not this time");
			}));
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(500), () -> fail("enigma")));
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100)));
		assertMessageEquals(error, "execution timed out after 50 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100), "Tempus Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 50 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100), () -> "Tempus" + " " + "Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 50 ms");
	}

}
