/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals

@Suppress("UNUSED_VARIABLE")
class AssertionsSamples {
    @Test fun assertThrowsSample() {
        val exception = assertThrows<IllegalArgumentException> {
            throw IllegalArgumentException("Talk to a duck")
        }
        assertEquals("Talk to a duck", exception.message)
    }

    @Test fun assertThrowsWithMessageSample() {
        val exception = assertThrows<IllegalArgumentException>("Should throw an Exception") {
            throw IllegalArgumentException("Talk to a duck")
        }
        assertEquals("Talk to a duck", exception.message)
    }

    @Test fun assertThrowsWithLazyMessageSample() {
        val exception = assertThrows<IllegalArgumentException>({ "Should throw an Exception" }) {
            throw IllegalArgumentException("Talk to a duck")
        }
        assertEquals("Talk to a duck", exception.message)
    }

    @Test fun assertDoesNotThrowSample() {
        val result = assertDoesNotThrow {
            // Code block that is expected to not throw an exception
        }
    }

    @Test fun assertDoesNotThrowWithMessageSample() {
        val result = assertDoesNotThrow("Should not throw an exception") {
            // Code block that is expected to not throw an exception
        }
    }

    @Test fun assertDoesNotThrowWithLazyMessageSample() {
        val result = assertDoesNotThrow({ "Should not throw an exception" }) {
            // Code block that is expected to not throw an exception
        }
    }

    @Test fun assertTimeoutSample() {
        val result = assertTimeout(Duration.ofSeconds(1)) {
            // Code block that is being timed.
        }
    }

    @Test fun assertTimeoutWithMessageSample() {
        val result = assertTimeout(Duration.ofSeconds(1), "Should only take one second") {
            // Code block that is being timed.
        }
    }

    @Test fun assertTimeoutWithLazyMessageSample() {
        val result = assertTimeout(Duration.ofSeconds(1), { "Should only take one second" }) {
            // Code block that is being timed.
        }
    }

    @Test fun assertTimeoutPreemptivelySample() {
        val result = assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            // Code block that is being timed.
        }
    }

    @Test fun assertTimeoutPreemptivelyWithMessageSample() {
        val result = assertTimeoutPreemptively(Duration.ofSeconds(1), "Should only take one second") {
            // Code block that is being timed.
        }
    }

    @Test fun assertTimeoutPreemptivelyWithLazyMessageSample() {
        val result = assertTimeoutPreemptively(Duration.ofSeconds(1), { "Should only take one second" }) {
            // Code block that is being timed.
        }
    }
}
