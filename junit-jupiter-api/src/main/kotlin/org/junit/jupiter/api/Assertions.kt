/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:API(status = EXPERIMENTAL, since = "5.1")

package org.junit.jupiter.api

import java.time.Duration
import java.util.Arrays
import java.util.function.Supplier
import java.util.stream.Stream
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.function.ThrowingSupplier

/**
 * @see Assertions.fail
 */
fun fail(message: String?, throwable: Throwable? = null): Nothing =
    Assertions.fail(message, throwable)

/**
 * @see Assertions.fail
 */
fun fail(message: (() -> String)?): Nothing =
    Assertions.fail(message)

/**
 * @see Assertions.fail
 */
fun fail(throwable: Throwable?): Nothing =
    Assertions.fail(throwable)

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: Stream<() -> Unit>) =
    assertAll(null, executables)

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: Stream<() -> Unit>) =
    Assertions.assertAll(heading, executables.map { Executable(it) })

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: Collection<() -> Unit>) =
    assertAll(executables.stream())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: Collection<() -> Unit>) =
    assertAll(heading, executables.stream())

/**
 * @see Assertions.assertAll
 */
fun assertAll(vararg executables: () -> Unit) =
    assertAll(Arrays.stream(executables))

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, vararg executables: () -> Unit) =
    assertAll(heading, Arrays.stream(executables))

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertThrowsSample
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(noinline executable: () -> Unit): T =
    Assertions.assertThrows(T::class.java, Executable(executable))

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertThrowsWithMessageSample
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(message: String, noinline executable: () -> Unit): T =
    assertThrows({ message }, executable)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertThrowsWithLazyMessageSample
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(noinline message: () -> String, noinline executable: () -> Unit): T =
    Assertions.assertThrows(T::class.java, Executable(executable), Supplier(message))

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertDoesNotThrowSample
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertDoesNotThrow(executable: () -> R): R =
    Assertions.assertDoesNotThrow(ThrowingSupplier(executable))

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertDoesNotThrowWithMessageSample
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertDoesNotThrow(message: String, executable: () -> R): R =
    assertDoesNotThrow({ message }, executable)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertDoesNotThrowWithLazyMessageSample
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertDoesNotThrow(message: () -> String, executable: () -> R): R =
    Assertions.assertDoesNotThrow(ThrowingSupplier(executable), Supplier(message))

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutSample
 * @see Assertions.assertTimeout
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutWithMessageSample
 * @see Assertions.assertTimeout
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, message: String, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable, message)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutWithLazyMessageSample
 * @see Assertions.assertTimeout
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, message: () -> String, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable, message)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutPreemptivelySample
 * @see Assertions.assertTimeoutPreemptively
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutPreemptivelyWithMessageSample
 * @see Assertions.assertTimeoutPreemptively
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, message: String, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable, message)

/**
 * @sample org.junit.jupiter.api.AssertionsSamples.assertTimeoutPreemptivelyWithLazyMessageSample
 * @see Assertions.assertTimeoutPreemptively
 * @param R the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, message: () -> String, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable, message)
