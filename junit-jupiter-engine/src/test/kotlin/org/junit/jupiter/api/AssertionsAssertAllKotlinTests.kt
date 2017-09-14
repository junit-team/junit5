/*
 * Copyright 2015-2017 the original author or authors.
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
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Unit tests for JUnit Jupiter [org.junit.jupiter.api] top-level assertion functions.
 *
 * @since 5.0
 */
class AssertionsAssertAllKotlinTests {

    // Bonus: no null check tests as these get handled by the compiler!

    @Test
    fun `assertAll with functions that do not throw exceptions`() {
        assertAll(
            { assertTrue(true) },
            { assertFalse(false) },
            { assertTrue(true) }
        )
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
    fun `assertAll with streamOf functions that throw AssertionErrors`() {
        val multipleFailuresError = assertThrows<MultipleFailuresError>("Should have thrown multiple errors") {
            assertAll(Stream.of({ assertFalse(true) }, { assertFalse(true) }))
        }
        assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError::class, AssertionFailedError::class)
    }

    @Test
    fun `assertThrows that does not have exception thrown does not throw KotlinNullPointer`() {
        val assertionMessage = "This will not throw an exception"
        val error = assertThrows<AssertionFailedError>("assertThrows did not throw the correct exception") {
            assertThrows<IllegalStateException>(assertionMessage) { }
            // This should never execute
            expectAssertionFailedError()
        }
        assertMessageStartsWith(error, assertionMessage)
    }

    companion object {
        fun assertExpectedExceptionTypes(
            multipleFailuresError: MultipleFailuresError,
            vararg exceptionTypes: KClass<out Throwable>) =
            AssertionsAssertAllTests.assertExpectedExceptionTypes(
                multipleFailuresError, *exceptionTypes.map { it.java }.toTypedArray())
    }
}
