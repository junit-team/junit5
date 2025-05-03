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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.opentest4j.AssertionFailedError
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.util.stream.Stream
import kotlin.coroutines.Continuation
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

class KotlinSuspendFunctionsTests {
    @Test
    fun regularTest() {
        val method =
            KotlinSuspendFunctionsTests::class.java.getDeclaredMethod("suspendFunction", String::class.java, Continuation::class.java)
        println(method.toGenericString())
        assertTrue(method.kotlinFunction!!.isSuspend)
        assertEquals(
            String::class.java,
            method.kotlinFunction!!
                .parameters
                .last()
                .type.javaType
        )
        assertEquals(Stream::class.java, (method.kotlinFunction!!.returnType.javaType as ParameterizedType).rawType)

        val exception =
            assertThrows<InvocationTargetException> {
                runBlocking {
                    method.kotlinFunction!!.callSuspend(this@KotlinSuspendFunctionsTests, "boom")
                }
            }
        assertNotNull(exception.cause)
        assertEquals(AssertionFailedError::class, exception.cause!!::class)
        assertEquals("boom", exception.cause!!.message)
    }

    @Suppress("unused")
    suspend fun suspendFunction(value: String): Stream<DynamicTest> {
        fail(value)
    }

    @Suppress("JUnitMalformedDeclaration")
    @ParameterizedTest
    @ValueSource(strings = ["foo", "bar"])
    suspend fun suspendingTest(message: String) {
        fail(message)
    }

    @Test
    fun manualCoroutineTest(): Unit =
        runBlocking {
            fail("boom")
        }
}
