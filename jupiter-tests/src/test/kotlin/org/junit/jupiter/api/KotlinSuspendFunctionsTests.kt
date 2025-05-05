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
import org.junit.jupiter.params.BeforeParameterizedClassInvocation
import org.junit.jupiter.params.Parameter
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ParameterizedClass
@ValueSource(ints = [1, 2])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinSuspendFunctionsTests {
    @BeforeParameterizedClassInvocation(injectArguments = false)
    suspend fun beforeInvocation() {
        suspendingPrintln("beforeInvocation")
    }

    @Parameter
    var parameter: Int = 0

    @Suppress("JUnitMalformedDeclaration")
    @BeforeEach
    suspend fun beforeEach() {
        suspendingPrintln("beforeEach")
    }

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

    private suspend fun suspendingFail(message: String): Nothing = fail(message)

    private suspend fun suspendingPrintln(message: String) = println(message)
}
