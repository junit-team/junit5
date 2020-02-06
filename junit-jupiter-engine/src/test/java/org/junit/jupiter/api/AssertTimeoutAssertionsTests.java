/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.ExceptionUtils;
import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertTimeoutAssertionsTests {

	private static ThreadLocal<AtomicBoolean> changed = ThreadLocal.withInitial(() -> new AtomicBoolean(false));

	private final Executable nix = () -> {
	};

	// --- executable ----------------------------------------------------------

	@Test
	void assertTimeoutForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);
		assertTimeout(ofMillis(500), () -> changed.get().set(true));
		assertTrue(changed.get().get(), "should have executed in the same thread");
		assertTimeout(ofMillis(500), nix, "message");
		assertTimeout(ofMillis(500), nix, () -> "message");
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
			() -> assertTimeout(ofMillis(10), this::nap));
		assertMessageStartsWith(error, "execution exceeded timeout of 10 ms by");
	}

	@Test
	void assertTimeoutWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(10), this::nap, "Tempus Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 10 ms by");
	}

	@Test
	void assertTimeoutWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(10), this::nap, () -> "Tempus" + " " + "Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 10 ms by");
	}

	// --- supplier ------------------------------------------------------------

	@Test
	void assertTimeoutForSupplierThatCompletesBeforeTheTimeout() {
		changed.get().set(false);
		String result = assertTimeout(ofMillis(500), () -> {
			changed.get().set(true);
			return "Tempus Fugit";
		});
		assertTrue(changed.get().get(), "should have executed in the same thread");
		assertEquals("Tempus Fugit", result);
		assertEquals("Tempus Fugit", assertTimeout(ofMillis(500), () -> "Tempus Fugit", "message"));
		assertEquals("Tempus Fugit", assertTimeout(ofMillis(500), () -> "Tempus Fugit", () -> "message"));
	}

	@Test
	void assertTimeoutForSupplierThatThrowsAnException() {
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			assertTimeout(ofMillis(500), () -> {
				ExceptionUtils.throwAsUncheckedException(new RuntimeException("not this time"));
				return "Tempus Fugit";
			});
		});
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutForSupplierThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> {
			assertTimeout(ofMillis(500), () -> {
				fail("enigma");
				return "Tempus Fugit";
			});
		});
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeout(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			});
		});
		assertMessageStartsWith(error, "execution exceeded timeout of 10 ms by");
	}

	@Test
	void assertTimeoutWithMessageForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeout(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			}, "Tempus Fugit");
		});
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 10 ms by");
	}

	@Test
	void assertTimeoutWithMessageSupplierForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeout(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			}, () -> "Tempus" + " " + "Fugit");
		});
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 10 ms by");
	}

	// -- executable - preemptively ---

	@Test
	void assertTimeoutPreemptivelyForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);
		assertTimeoutPreemptively(ofMillis(500), () -> changed.get().set(true));
		assertFalse(changed.get().get(), "should have executed in a different thread");
		assertTimeoutPreemptively(ofMillis(500), nix, "message");
		assertTimeoutPreemptively(ofMillis(500), nix, () -> "message");
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
			() -> assertTimeoutPreemptively(ofMillis(10), this::waitForInterrupt));
		assertMessageEquals(error, "execution timed out after 10 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(10), this::waitForInterrupt, "Tempus Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 10 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(10), this::waitForInterrupt, () -> "Tempus" + " " + "Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 10 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageSupplierForExecutableThatCompletesBeforeTheTimeout() {
		assertTimeoutPreemptively(ofMillis(500), nix, () -> "Tempus" + " " + "Fugit");
	}

	// -- supplier - preemptively ---

	@Test
	void assertTimeoutPreemptivelyForSupplierThatCompletesBeforeTheTimeout() {
		changed.get().set(false);
		String result = assertTimeoutPreemptively(ofMillis(500), () -> {
			changed.get().set(true);
			return "Tempus Fugit";
		});
		assertFalse(changed.get().get(), "should have executed in a different thread");
		assertEquals("Tempus Fugit", result);
		assertEquals("Tempus Fugit", assertTimeoutPreemptively(ofMillis(500), () -> "Tempus Fugit", "message"));
		assertEquals("Tempus Fugit", assertTimeoutPreemptively(ofMillis(500), () -> "Tempus Fugit", () -> "message"));
	}

	@Test
	void assertTimeoutPreemptivelyForSupplierThatThrowsAnException() {
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			assertTimeoutPreemptively(ofMillis(500), () -> {
				ExceptionUtils.throwAsUncheckedException(new RuntimeException("not this time"));
				return "Tempus Fugit";
			});
		});
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutPreemptivelyForSupplierThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(ofMillis(500), () -> {
				fail("enigma");
				return "Tempus Fugit";
			});
		});
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutPreemptivelyForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			});
		});
		assertMessageEquals(error, "execution timed out after 10 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			}, "Tempus Fugit");
		});
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 10 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageSupplierForSupplierThatCompletesAfterTheTimeout() {
		AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> {
			assertTimeoutPreemptively(ofMillis(10), () -> {
				nap();
				return "Tempus Fugit";
			}, () -> "Tempus" + " " + "Fugit");
		});
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 10 ms");
	}

	/**
	 * Take a nap for 100 milliseconds.
	 */
	private void nap() throws InterruptedException {
		long start = System.currentTimeMillis();
		// workaround for imprecise clocks (yes, Windows, I'm talking about you)
		do {
			Thread.sleep(100);
		} while (System.currentTimeMillis() - start < 100);
	}

	private void waitForInterrupt() {
		try {
			new CountDownLatch(1).await();
		}
		catch (InterruptedException ignore) {
			// ignore
		}
	}

}
