/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith
import org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError
import org.opentest4j.AssertionFailedError
import org.opentest4j.MultipleFailuresError
import java.lang.AssertionError

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
                multipleFailuresError, *exceptionTypes.map { it.java }.toTypedArray())
    }
}
