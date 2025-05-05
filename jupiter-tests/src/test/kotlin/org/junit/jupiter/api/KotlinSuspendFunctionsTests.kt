/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinSuspendFunctionsTests {
    @Test
    fun regularTest() {
        runBlocking {
            suspendingFail("regular")
        }
    }

    @Suppress("JUnitMalformedDeclaration")
    @ParameterizedTest
    @ValueSource(strings = ["foo", "bar"])
    suspend fun suspendingTest(message: String) {
        suspendingFail(message)
    }

    @Test
    fun manualCoroutineTest(): Unit =
        runBlocking {
            suspendingFail("manual")
        }

    private suspend fun suspendingFail(message: String): Nothing = fail("boom")
}
