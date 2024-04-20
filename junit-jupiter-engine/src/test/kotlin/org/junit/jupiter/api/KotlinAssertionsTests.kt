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
    fun `assertThrows with message supplier`() {
        val valueInMessageSupplier: Int

        assertThrows<AssertionError>({
            valueInMessageSupplier = 20 // Val can be assigned in the message supplier lambda.

            "should fail"
        }) { fail("message") }
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
    fun `assertDoesNotThrow with value initialization in lambda`() {
        val value: Int

        assertDoesNotThrow { value = 10 }

        assertEquals(10, value)
    }

    @Test
    fun `assertDoesNotThrow with message and value initialization in lambda`() {
        val value: Int

        assertDoesNotThrow("message") { value = 10 }

        assertEquals(10, value)
    }

    @Test
    fun `assertDoesNotThrow with message supplier and value initialization in lambda`() {
        val value: Int
        val valueInMessageSupplier: Int

        assertDoesNotThrow({
            valueInMessageSupplier = 20 // Val can be assigned in the message supplier lambda.

            "message"
        }) { value = 10 }

        assertEquals(10, value)
    }

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

    @Test
    fun `assertNotNull with compiler smart cast`() {
        val nullableString: String? = "string"

        assertNotNull(nullableString)
        assertFalse(nullableString.isEmpty()) // A smart cast to a non-nullable object.
    }

    @Test
    fun `assertNotNull with message and compiler smart cast`() {
        val nullableString: String? = "string"

        assertNotNull(nullableString, "nullableString is null")
        assertFalse(nullableString.isEmpty()) // A smart cast to a non-nullable object.
    }

    @Test
    fun `assertNotNull with message supplier and compiler smart cast`() {
        val nullableString: String? = "string"

        val valueInMessageSupplier: Int

        assertNotNull(nullableString) {
            valueInMessageSupplier = 20 // Val can be assigned in the message supplier lambda.

            "nullableString is null"
        }

        assertFalse(nullableString.isEmpty()) // A smart cast to a non-nullable object.
    }

    @Test
    fun `assertNull with compiler smart cast`() {
        val nullableString: String? = null

        assertNull(nullableString)
        // Even safe call is not allowed because compiler knows that nullableString is always null.
        // nullableString?.isEmpty()
    }

    @Test
    fun `assertNull with message and compiler smart cast`() {
        val nullableString: String? = null

        assertNull(nullableString, "nullableString is not null")
        // Even safe call is not allowed because compiler knows that nullableString is always null.
        // nullableString?.isEmpty()
    }

    @Test
    fun `assertNull with message supplier and compiler smart cast`() {
        val nullableString: String? = null

        val valueInMessageSupplier: Int

        assertNull(nullableString) {
            valueInMessageSupplier = 20 // Val can be assigned in the message supplier lambda.

            "nullableString is not null"
        }

        // Even safe call is not allowed because compiler knows that nullableString is always null.
        // nullableString?.isEmpty()
    }

    @Test
    fun `assertInstanceOf with compiler smart cast`() {
        val maybeString: Any = "string"

        assertInstanceOf<String>(maybeString)
        assertFalse(maybeString.isEmpty()) // A smart cast to a String object.
    }

    @Test
    fun `assertInstanceOf with compiler nullable smart cast`() {
        val maybeString: Any? = "string"

        assertInstanceOf<String>(maybeString)
        assertFalse(maybeString.isEmpty()) // A smart cast to a non-nullable String object.
    }

    @Test
    fun `assertInstanceOf with a null value`() {
        val error = assertThrows<AssertionFailedError> {
            assertInstanceOf<String>(null)
        }

        assertMessageStartsWith(error, "Unexpected null value")
    }

    @Test
    fun `assertInstanceOf with message and compiler smart cast`() {
        val maybeString: Any = "string"

        assertInstanceOf<String>(maybeString, "maybeString is not an instance of String")
        assertFalse(maybeString.isEmpty()) // A smart cast to a String object.
    }

    @Test
    fun `assertInstanceOf with message supplier and compiler smart cast`() {
        val maybeString: Any = "string"

        val valueInMessageSupplier: Int

        assertInstanceOf<String>(maybeString) {
            valueInMessageSupplier = 20 // Val can be assigned in the message supplier lambda.

            "maybeString is not an instance of String"
        }

        assertFalse(maybeString.isEmpty()) // A smart cast to a String object.
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
