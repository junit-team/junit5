/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:API(status = STABLE, since = "5.7")

package org.junit.jupiter.api

import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.apiguardian.api.API.Status.STABLE
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.function.ThrowingSupplier
import java.time.Duration
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * @see Assertions.fail
 */
fun fail(message: String?, throwable: Throwable? = null): Nothing =
    Assertions.fail<Nothing>(message, throwable)

/**
 * @see Assertions.fail
 */
fun fail(message: (() -> String)?): Nothing =
    Assertions.fail<Nothing>(message)

/**
 * @see Assertions.fail
 */
fun fail(throwable: Throwable?): Nothing =
    Assertions.fail<Nothing>(throwable)

/**
 * [Stream] of functions to be executed.
 */
private typealias ExecutableStream = Stream<() -> Unit>
private fun ExecutableStream.convert() = map { Executable(it) }

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: ExecutableStream) =
    Assertions.assertAll(executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: ExecutableStream) =
    Assertions.assertAll(heading, executables.convert())

/**
 * [Collection] of functions to be executed.
 */
private typealias ExecutableCollection = Collection<() -> Unit>
private fun ExecutableCollection.convert() = map { Executable(it) }

/**
 * @see Assertions.assertAll
 */
fun assertAll(executables: ExecutableCollection) =
    Assertions.assertAll(executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, executables: ExecutableCollection) =
    Assertions.assertAll(heading, executables.convert())

/**
 * @see Assertions.assertAll
 */
fun assertAll(vararg executables: () -> Unit) =
    assertAll(executables.toList().stream())

/**
 * @see Assertions.assertAll
 */
fun assertAll(heading: String?, vararg executables: () -> Unit) =
    assertAll(heading, executables.toList().stream())

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException> {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T {
    val throwable: Throwable? = try {
        executable()
    } catch (caught: Throwable) {
        caught
    } as? Throwable

    return Assertions.assertThrows(T::class.java) {
        if (throwable != null) {
            throw throwable
        }
    }
}

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException>("Should throw an Exception") {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(message: String, executable: () -> Unit): T =
    assertThrows({ message }, executable)

/**
 * Example usage:
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException>({ "Should throw an Exception" }) {
 *     throw IllegalArgumentException("Talk to a duck")
 * }
 * assertEquals("Talk to a duck", exception.message)
 * ```
 * @see Assertions.assertThrows
 */
inline fun <reified T : Throwable> assertThrows(noinline message: () -> String, executable: () -> Unit): T {
    val throwable: Throwable? = try {
        executable()
    } catch (caught: Throwable) {
        caught
    } as? Throwable

    return Assertions.assertThrows(
        T::class.java,
        Executable {
            if (throwable != null) {
                throw throwable
            }
        },
        Supplier(message)
    )
}

/**
 * Example usage:
 * ```kotlin
 * val result = assertDoesNotThrow {
 *     // Code block that is expected to not throw an exception
 * }
 * ```
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
inline fun <R> assertDoesNotThrow(executable: () -> R): R =
    Assertions.assertDoesNotThrow(evaluateAndWrap(executable))

/**
 * Example usage:
 * ```kotlin
 * val result = assertDoesNotThrow("Should not throw an exception") {
 *     // Code block that is expected to not throw an exception
 * }
 * ```
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
inline fun <R> assertDoesNotThrow(message: String, executable: () -> R): R =
    assertDoesNotThrow({ message }, executable)

/**
 * Example usage:
 * ```kotlin
 * val result = assertDoesNotThrow({ "Should not throw an exception" }) {
 *     // Code block that is expected to not throw an exception
 * }
 * ```
 * @see Assertions.assertDoesNotThrow
 * @param R the result type of the [executable]
 */
@API(status = EXPERIMENTAL, since = "5.5")
inline fun <R> assertDoesNotThrow(noinline message: () -> String, executable: () -> R): R =
    Assertions.assertDoesNotThrow(
        evaluateAndWrap(executable),
        Supplier(message)
    )

@PublishedApi
internal inline fun <R> evaluateAndWrap(executable: () -> R): ThrowingSupplier<R> = try {
    val result = executable()
    ThrowingSupplier { result }
} catch (throwable: Throwable) {
    ThrowingSupplier { throw throwable }
}

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeout(Duration.seconds(1)) {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeout
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable)

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeout(Duration.seconds(1), "Should only take one second") {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeout
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, message: String, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable, message)

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeout(Duration.seconds(1), { "Should only take one second" }) {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeout
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeout(timeout: Duration, message: () -> String, executable: () -> R): R =
    Assertions.assertTimeout(timeout, executable, message)

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeoutPreemptively(Duration.seconds(1)) {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeoutPreemptively
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable)

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeoutPreemptively(Duration.seconds(1), "Should only take one second") {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeoutPreemptively
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, message: String, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable, message)

/**
 * Example usage:
 * ```kotlin
 * val result = assertTimeoutPreemptively(Duration.seconds(1), { "Should only take one second" }) {
 *     // Code block that is being timed.
 * }
 * ```
 * @see Assertions.assertTimeoutPreemptively
 * @paramR the result of the [executable].
 */
@API(status = EXPERIMENTAL, since = "5.5")
fun <R> assertTimeoutPreemptively(timeout: Duration, message: () -> String, executable: () -> R): R =
    Assertions.assertTimeoutPreemptively(timeout, executable, message)
