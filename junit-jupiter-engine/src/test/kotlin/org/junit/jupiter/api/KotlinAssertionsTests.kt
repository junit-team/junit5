/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals
import org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith
import org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.opentest4j.AssertionFailedError
import org.opentest4j.MultipleFailuresError
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Unit tests for JUnit Jupiter [org.junit.jupiter.api] top-level assertion functions.
 */
class KotlinAssertionsTests {

    // Bonus: no null check tests as these get handled by the compiler!

    @Test
    fun `assertAll with functions that do not throw exceptions`() {
        assertAll(Stream.of({ assertTrue(true) }, { assertFalse(false) }))
        assertAll("heading", Stream.of({ assertTrue(true) }, { assertFalse(false) }))
        assertAll(setOf({ assertTrue(true) }, { assertFalse(false) }))
        assertAll("heading", setOf({ assertTrue(true) }, { assertFalse(false) }))
        assertAll({ assertTrue(true) }, { assertFalse(false) })
        assertAll("heading", { assertTrue(true) }, { assertFalse(false) })
    }

    @Test
    fun `assertAll with functions that throw AssertionErrors`() {
        val multipleFailuresError = assertThrows<MultipleFailuresError> {
            assertAll(
                { assertFalse(true) },
                { assertFalse(true) }
            )
        }
        assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError::class, AssertionFailedError::class)
    }

    @Test
    fun `assertThrows and fail`() {
        assertThrows<AssertionError> { fail("message") }
        assertThrows<AssertionError> { fail("message", AssertionError()) }
        assertThrows<AssertionError> { fail("message", null) }
        assertThrows<AssertionError>("should fail") { fail({ "message" }) }
        assertThrows<AssertionError>({ "should fail" }) { fail(AssertionError()) }
        assertThrows<AssertionError>({ "should fail" }) { fail(null as Throwable?) }
    }

    @Test
    fun `expected context exception testing`() = runBlocking<Unit> {
        assertThrows<AssertionError>("Should fail async") {
            suspend { fail("Should fail async") }()
        }
    }

    @TestFactory
    fun assertDoesNotThrow(): Stream<out DynamicNode> = Stream.of(
        dynamicContainer(
            "succeeds when no exception thrown",
            Stream.of(
                dynamicTest("for no arguments variant") {
                    val actual = assertDoesNotThrow { 1 }
                    assertEquals(1, actual)
                },
                dynamicTest("for no arguments variant (suspended)") {
                    runBlocking {
                        val actual = assertDoesNotThrow { suspend { 1 }() }
                        assertEquals(1, actual)
                    }
                },
                dynamicTest("for message variant") {
                    val actual = assertDoesNotThrow("message") { 2 }
                    assertEquals(2, actual)
                },
                dynamicTest("for message variant (suspended)") {
                    runBlocking {
                        val actual = assertDoesNotThrow("message") { suspend { 2 }() }
                        assertEquals(2, actual)
                    }
                },
                dynamicTest("for message supplier variant") {
                    val actual = assertDoesNotThrow({ "message" }) { 3 }
                    assertEquals(3, actual)
                },
                dynamicTest("for message supplier variant (suspended)") {
                    runBlocking {
                        val actual = assertDoesNotThrow({ "message" }) { suspend { 3 }() }
                        assertEquals(3, actual)
                    }
                }
            )
        ),
        dynamicContainer(
            "fails when an exception is thrown",
            Stream.of(
                dynamicTest("for no arguments variant") {
                    val exception = assertThrows<AssertionError> {
                        assertDoesNotThrow {
                            fail("fail")
                        }
                    }
                    assertMessageEquals(
                        exception,
                        "Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                    )
                },
                dynamicTest("for no arguments variant (suspended)") {
                    runBlocking {
                        val exception = assertThrows<AssertionError> {
                            assertDoesNotThrow {
                                suspend { fail("fail") }()
                            }
                        }
                        assertMessageEquals(
                            exception,
                            "Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                        )
                    }
                },
                dynamicTest("for message variant") {
                    val exception = assertThrows<AssertionError> {
                        assertDoesNotThrow("Does not throw") {
                            fail("fail")
                        }
                    }
                    assertMessageEquals(
                        exception,
                        "Does not throw ==> Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                    )
                },
                dynamicTest("for message variant (suspended)") {
                    runBlocking {
                        val exception = assertThrows<AssertionError> {
                            assertDoesNotThrow("Does not throw") {
                                suspend { fail("fail") }()
                            }
                        }
                        assertMessageEquals(
                            exception,
                            "Does not throw ==> Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                        )
                    }
                },
                dynamicTest("for message supplier variant") {
                    val exception = assertThrows<AssertionError> {
                        assertDoesNotThrow({ "Does not throw" }) {
                            fail("fail")
                        }
                    }
                    assertMessageEquals(
                        exception,
                        "Does not throw ==> Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                    )
                },
                dynamicTest("for message supplier variant (suspended)") {
                    runBlocking {
                        val exception = assertThrows<AssertionError> {
                            assertDoesNotThrow({ "Does not throw" }) {
                                suspend { fail("fail") }()
                            }
                        }
                        assertMessageEquals(
                            exception,
                            "Does not throw ==> Unexpected exception thrown: org.opentest4j.AssertionFailedError: fail"
                        )
                    }
                }
            )
        )
    )

    @Test
    fun `assertAll with stream of functions that throw AssertionErrors`() {
        val multipleFailuresError = assertThrows<MultipleFailuresError>("Should have thrown multiple errors") {
            assertAll(Stream.of({ assertFalse(true) }, { assertFalse(true) }))
        }
        assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError::class, AssertionFailedError::class)
    }

    @Test
    fun `assertAll with collection of functions that throw AssertionErrors`() {
        val multipleFailuresError = assertThrows<MultipleFailuresError>("Should have thrown multiple errors") {
            assertAll(setOf({ assertFalse(true) }, { assertFalse(true) }))
        }
        assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError::class, AssertionFailedError::class)
    }

    @Test
    fun `assertThrows with function that does not throw an exception`() {
        val assertionMessage = "This will not throw an exception"
        val error = assertThrows<AssertionFailedError>("assertThrows did not throw the correct exception") {
            assertThrows<IllegalStateException>(assertionMessage) { }
            // This should never execute:
            expectAssertionFailedError()
        }
        assertMessageStartsWith(error, assertionMessage)
    }

    companion object {
        fun assertExpectedExceptionTypes(
            multipleFailuresError: MultipleFailuresError,
            vararg exceptionTypes: KClass<out Throwable>
        ) =
            AssertAllAssertionsTests.assertExpectedExceptionTypes(
                multipleFailuresError,
                *exceptionTypes.map { it.java }.toTypedArray()
            )
    }
}
