/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:JvmName("TestBuilder")
@file:Suppress(JAVA_ONLY_HACK, "NOTHING_TO_INLINE")

package org.junit.jupiter.api

import java.util.function.BiFunction
import java.util.stream.Stream
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.function.ThrowingConsumer
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer10
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer11
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer12
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer13
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer14
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer15
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer16
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer17
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer18
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer19
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer2
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer20
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer21
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer22
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer3
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer4
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer5
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer6
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer7
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer8
import org.junit.jupiter.api.function.ThrowingConsumers.ThrowingConsumer9

/**
 * The [TestCaseIterator] lazily builds a [DynamicTest] with the given [test]
 * function for each of the supplied [cases] when iterated. It is to be used in
 * conjunction with the [@TestFactory][TestFactory] annotation. Use the [testOf]
 * family of static factory functions to build instances of this class.
 *
 * Use the [TestCases] interface as the return type for the test function that is
 * annotated with [@TestFactory][TestFactory]. This avoids the unnecessary
 * repetition of the actual [type][T] in the signature.
 *
 * Note that the [testOf] functions cannot use [TestCases] as their return type
 * because it would make it impossible to provide a type-safe
 * [display name generator][named] functionality then.
 *
 * ## Examples
 *
 * ### Java
 * ```java
 * import java.util.Arrays;
 *
 * import static org.junit.jupiter.api.Assertions.assertEquals;
 * import static org.junit.jupiter.api.Assertions.assertTrue;
 * import static org.junit.jupiter.api.TestBuilder.testOf;
 * import static org.junit.jupiter.api.TestCaseBuilder.caseOf;
 *
 * final class JavaExamples {
 *     @TestFactory
 *     Tests collections() {
 *         // There are overloads for many different collection types available
 *         // that may act as the source for the cases of the test.
 *         return testOf(
 *             new int[]{1, 2, 3},
 *             it -> assertTrue(it > 0)
 *         );
 *     }
 *
 *     enum CardinalDirection {
 *         North(0), East(90), South(180), West(270);
 *
 *         public final int degree;
 *
 *         CardinalDirection(final int degree) {
 *             this.degree = degree;
 *         }
 *     }
 *
 *     @TestFactory
 *     Tests enums() {
 *         return testOf(
 *             CardinalDirection.class,
 *             it -> assertTrue(it.degree >= 0 && it.degree <= 360)
 *         );
 *     }
 *
 *     @TestFactory
 *     Tests parameterized() {
 *         // There are type-safe overloads from 1 to 22 parameters.
 *         return testOf(
 *             (str, num, list) -> {
 *                 assertEquals(5, str.length());
 *                 assertTrue(num >= 1 && num <= 2);
 *                 assertEquals(2, list.size());
 *             },
 *             caseOf("apple", 1, Arrays.asList('a', 'b')),
 *             caseOf("lemon", 2, Arrays.asList('x', 'y'))
 *         );
 *     }
 * }
 * ```
 *
 * ### Kotlin
 * ```kotlin
 * import org.junit.jupiter.api.Assertions.assertEquals
 * import org.junit.jupiter.api.Assertions.assertTrue
 *
 * private class KotlinExamples {
 *     // There are overloads for many different collection types available that
 *     // may act as the source for the cases of the test.
 *     @TestFactory fun collections() =
 *         testOf(sequence {
 *             // Using a sequence as the source makes it completely async.
 *             yield(1)
 *             yield(2)
 *             yield(3)
 *         }) { assertTrue(it > 0) }
 *
 *     @TestFactory fun ranges() =
 *         testOf(1..3) { assertTrue(it > 0) }
 *
 *     enum class CardinalDirection(val degree: Int) {
 *         North(0), East(90), South(180), West(270)
 *     }
 *
 *     @TestFactory fun enums() =
 *         testOf<CardinalDirection> {
 *             assertTrue(it.degree in 0..360)
 *         }
 *
 *     // There are type-safe overloads from 1 to 22 parameters.
 *     @TestFactory fun parameterized() =
 *         testOf(
 *             case("apple", 1, listOf('a', 'b')),
 *             case("lemon", 2, listOf('x', 'y'))
 *         ) { str, num, list ->
 *             assertEquals(5, str.length)
 *             assertTrue(num in 1..2)
 *             assertEquals(2, list.size)
 *         }
 * }
 * ```
 *
 * @since 5.7
 * @see org.junit.jupiter.api.case
 * @see org.junit.jupiter.api.TestCase
 * @see org.junit.jupiter.api.TestFactory
 * @see org.junit.jupiter.api.testOf
 * @see org.junit.jupiter.api.TestCases
 */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCaseIterator<T> @PublishedApi internal constructor(
    private val cases: Iterator<T>,
    private val test: (T) -> Unit
) : TestCases {
    private var i = 1
    private var name: ((Int, T) -> String)? = null

    override fun hasNext() =
        cases.hasNext()

    override fun next(): DynamicTest =
        cases.next().let { case ->
            dynamicTest(name?.invoke(i++, case) ?: "[${i++}] $case") {
                test(case)
            }
        }

    /**
     * Use the given custom display name generator to all parameters that are to
     * be tested with this parameterized test.
     *
     * The given function will receive the 1-based index of the parameters that
     * are to be tested as the first argument and the actual parameters as the
     * second argument. The formatting of individual parameters can be further
     * customized through the [TestCase.named] function for each individual
     * parameter collection.
     *
     * The default generator encloses the index in brackets and joins the
     * parameters after calling their respective [toString][Any.toString] with
     * a comma, e.g. `params("p1", "p2")` will result in `[1] p1, p2`.
     *
     * ## Examples
     * ```kotlin
     * @TestFactory fun isPalindrom() =
     *     testOf(case("mum"), case("dad")) {
     *         assertEquals(it.reversed(), it)
     *     } named { _, case -> case }
     * ```
     *
     * The above would result in the following output with the `ConsoleLauncher`
     * and the Unicode theme:
     *
     * ```
     * isPalindrom ✔
     * ├─ mum ✔
     * └─ dad ✔
     * ```
     *
     * Instead of the default output that would be:
     *
     * ```
     * isPalindrom ✔
     * ├─ [1] mum ✔
     * └─ [2] dad ✔
     * ```
     *
     * The second argument passed to the [name] function provides access to each
     * individual parameter of the case that is to be tested and thus allows for
     * sophisticated customization since any library in the classpath is
     * available for transforming the parameters. The various subclasses of
     * [TestCase] all support destructuring that makes sure that the individual
     * parameters can be named properly.
     *
     * ```kotlin
     * @TestFactory fun `Magic constant`() =
     *     testOf(
     *         case("Pi", 3.1415926535897932384626433),
     *         case("Euler‘s Number", 2.71828)
     *     ) { assertTrue(true) } named { i, (name, x) ->
     *         "#$i $name := %.2f".format(x)
     *     }
     * ```
     *
     * ```
     * Magic constant ✔
     * ├─ #1 Pi := 3.14 ✔
     * └─ #2 Euler‘s Number := 2.71 ✔
     * ```
     *
     * The above could be combined with [TestCase.named] if the name is not
     * required for the actual test:
     *
     * ```kotlin
     * @TestFactory fun `Magic constant`() =
     *     testOf(
     *         case(3.1415926535897932384626433) named "Pi",
     *         case(2.71828) named "Euler‘s Number"
     *     ) { assertTrue(true) } named { i, case ->
     *         val (x) = case // or access it with case.p1
     *         // The toString function of the case returns the name that was
     *         // given to it.
     *         "#$i $case := %.2f".format(x)
     *     }
     * ```
     *
     * @see org.junit.jupiter.api.TestCase.named
     */
    @JvmSynthetic
    infix fun named(name: (i: Int, case: T) -> String) =
        apply { this.name = name }

    /**
     * Use the given custom display name generator to all parameters that are to
     * be tested with this parameterized test.
     *
     * The given function will receive the 1-based index of the parameters that
     * are to be tested as the first argument and the actual parameters as the
     * second argument. The formatting of individual parameters can be further
     * customized through the [TestCase.named] function for each individual
     * parameter collection.
     *
     * The default generator encloses the index in brackets and joins the
     * parameters after calling their respective [toString][Any.toString] with
     * a comma, e.g. `params("p1", "p2")` will result in `[1] p1, p2`.
     *
     * ### Examples
     * ```java
     * @TestFactory
     * TestCases isPalindromTest() {
     *     return testOf(
     *         it -> assertTrue(isPalindrom(it)),
     *         caseOf("mum"),
     *         caseOf("dad")
     *     ).named((i, case) -> case);
     * }
     * ```
     *
     * The above would result in the following output with the `ConsoleLauncher`
     * and the Unicode theme:
     *
     * ```
     * isPalindrom ✔
     * ├─ mum ✔
     * └─ dad ✔
     * ```
     *
     * Instead of the default output that would be:
     *
     * ```
     * isPalindrom ✔
     * ├─ [1] mum ✔
     * └─ [2] dad ✔
     * ```
     *
     * The second argument passed to the [name] function provides access to each
     * individual parameter of the case that is to be tested and thus allows for
     * sophisticated customization since any library in the classpath is
     * available for transforming the parameters.
     *
     * ```java
     * @TestFactory
     * TestCases magicConstants() {
     *     return testOf(
     *         (name, x) -> assertTrue(true),
     *         caseOf("Pi", 3.1415926535897932384626433),
     *         caseOf("Euler‘s Number", 2.71828)
     *     ).named((i, case) -> format("#%d %s := %.2f", i, case.p1, case.p2));
     * }
     * ```
     *
     * ```
     * Magic constant ✔
     * ├─ #1 Pi := 3.14 ✔
     * └─ #2 Euler‘s Number := 2.71 ✔
     * ```
     *
     * The above could be combined with [TestCase.named] if the name is not
     * required for the actual test:
     *
     * ```java
     * @TestFactory
     * TestCases magicConstants() {
     *     return testOf(
     *         (name, x) -> assertTrue(true),
     *         caseOf(3.1415926535897932384626433).named("Pi"),
     *         caseOf(2.71828).named("Euler‘s Number")
     *     ).named((i, case) -> format("#%d %s := %.2f", i, case.toString(), case.p1));
     * }
     * ```
     *
     * @see org.junit.jupiter.api.TestCase.named
     */
    @SinceKotlin(JAVA_ONLY)
    fun named(name: BiFunction<Int, T, String>) =
        named(name::apply)
}

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [sequence].
 *
 * Using a sequence as data provider makes the complete test factory lazy since
 * yield is executed only if the iterator is advanced. This is ideal if the
 * values require a lot of memory.
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun <E> testOf(sequence: Sequence<E>, noinline test: (E) -> Unit) =
    TestCaseIterator(sequence.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [stream].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
fun <E> testOf(stream: Stream<E>, test: (E) -> Unit) =
    TestCaseIterator(stream.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [stream].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun <E> testOf(stream: Stream<E>, test: ThrowingConsumer<E>) =
    TestCaseIterator(stream.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun <E> testOf(array: Array<out E>, noinline test: (E) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun <E> testOf(array: Array<out E>, test: ThrowingConsumer<E>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: BooleanArray, noinline test: (Boolean) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: BooleanArray, test: ThrowingConsumer<Boolean>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: ByteArray, noinline test: (Byte) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: ByteArray, test: ThrowingConsumer<Byte>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: CharArray, noinline test: (Char) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: CharArray, test: ThrowingConsumer<Char>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: DoubleArray, noinline test: (Double) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: DoubleArray, test: ThrowingConsumer<Double>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: FloatArray, noinline test: (Float) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: FloatArray, test: ThrowingConsumer<Float>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: IntArray, noinline test: (Int) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: IntArray, test: ThrowingConsumer<Int>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: LongArray, noinline test: (Long) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: LongArray, test: ThrowingConsumer<Long>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(array: ShortArray, noinline test: (Short) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun testOf(array: ShortArray, test: ThrowingConsumer<Short>) =
    TestCaseIterator(array.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@ExperimentalUnsignedTypes
@JvmSynthetic
inline fun testOf(array: UIntArray, noinline test: (UInt) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@ExperimentalUnsignedTypes
@JvmSynthetic
inline fun testOf(array: ULongArray, noinline test: (ULong) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each element of the given [array].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@ExperimentalUnsignedTypes
@JvmSynthetic
inline fun testOf(array: UShortArray, noinline test: (UShort) -> Unit) =
    TestCaseIterator(array.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [iterable].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun <E> testOf(iterable: Iterable<E>, noinline test: (E) -> Unit) =
    TestCaseIterator(iterable.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [iterable].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun <E> testOf(iterable: Iterable<E>, test: ThrowingConsumer<E>) =
    TestCaseIterator(iterable.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [iterator].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun <E> testOf(iterator: Iterator<E>, noinline test: (E) -> Unit) =
    TestCaseIterator(iterator, test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [element][E] of the given [iterator].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun <E> testOf(iterator: Iterator<E>, test: ThrowingConsumer<E>) =
    TestCaseIterator(iterator, test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [value][enumValues] of the given [enum][E].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun <reified E : Enum<E>> testOf(noinline test: (E) -> Unit) =
    TestCaseIterator(enumValues<E>().iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each [value][enumValues] of the given [enum][E].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@SinceKotlin(JAVA_ONLY)
fun <E : Enum<E>> testOf(enum: Class<E>, test: ThrowingConsumer<E>) =
    TestCaseIterator(enum.enumConstants.iterator(), test::accept)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each value in the given [range].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(range: CharRange, noinline test: (Char) -> Unit) =
    TestCaseIterator(range.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each value in the given [range].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(range: IntRange, noinline test: (Int) -> Unit) =
    TestCaseIterator(range.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each value in the given [range].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@JvmSynthetic
inline fun testOf(range: LongRange, noinline test: (Long) -> Unit) =
    TestCaseIterator(range.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each value in the given [range].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@ExperimentalUnsignedTypes
@JvmSynthetic
inline fun testOf(range: UIntRange, noinline test: (UInt) -> Unit) =
    TestCaseIterator(range.iterator(), test)

/**
 * Build a [TestCaseIterator] that can be used with [@TestFactory][TestFactory]
 * and [test] each value in the given [range].
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@ExperimentalUnsignedTypes
@JvmSynthetic
inline fun testOf(range: ULongRange, noinline test: (ULong) -> Unit) =
    TestCaseIterator(range.iterator(), test)

// The arity of 22 was not chosen arbitrarily, it's the number Kotlin uses for
// its lambdas: https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/runtime/kotlin/jvm/functions/Functions.kt
//
// @formatter:off
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1> testOf(vararg cases: TestCase1<P1>, crossinline test: (P1) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2> testOf(vararg cases: TestCase2<P1, P2>, crossinline test: (P1, P2) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3> testOf(vararg cases: TestCase3<P1, P2, P3>, crossinline test: (P1, P2, P3) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4> testOf(vararg cases: TestCase4<P1, P2, P3, P4>, crossinline test: (P1, P2, P3, P4) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5> testOf(vararg cases: TestCase5<P1, P2, P3, P4, P5>, crossinline test: (P1, P2, P3, P4, P5) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6> testOf(vararg cases: TestCase6<P1, P2, P3, P4, P5, P6>, crossinline test: (P1, P2, P3, P4, P5, P6) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7> testOf(vararg cases: TestCase7<P1, P2, P3, P4, P5, P6, P7>, crossinline test: (P1, P2, P3, P4, P5, P6, P7) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8> testOf(vararg cases: TestCase8<P1, P2, P3, P4, P5, P6, P7, P8>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9> testOf(vararg cases: TestCase9<P1, P2, P3, P4, P5, P6, P7, P8, P9>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> testOf(vararg cases: TestCase10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> testOf(vararg cases: TestCase11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> testOf(vararg cases: TestCase12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> testOf(vararg cases: TestCase13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> testOf(vararg cases: TestCase14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> testOf(vararg cases: TestCase15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> testOf(vararg cases: TestCase16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> testOf(vararg cases: TestCase17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> testOf(vararg cases: TestCase18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> testOf(vararg cases: TestCase19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> testOf(vararg cases: TestCase20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21> testOf(vararg cases: TestCase21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20, it.p21) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @JvmSynthetic inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22> testOf(vararg cases: TestCase22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>, crossinline test: (P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22) -> Unit) = TestCaseIterator(cases.iterator()) { test(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20, it.p21, it.p22) }

/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1> testOf(test: ThrowingConsumer<P1>, vararg cases: TestCase1<P1>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2> testOf(test: ThrowingConsumer2<P1, P2>, vararg cases: TestCase2<P1, P2>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3> testOf(test: ThrowingConsumer3<P1, P2, P3>, vararg cases: TestCase3<P1, P2, P3>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4> testOf(test: ThrowingConsumer4<P1, P2, P3, P4>, vararg cases: TestCase4<P1, P2, P3, P4>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5> testOf(test: ThrowingConsumer5<P1, P2, P3, P4, P5>, vararg cases: TestCase5<P1, P2, P3, P4, P5>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6> testOf(test: ThrowingConsumer6<P1, P2, P3, P4, P5, P6>, vararg cases: TestCase6<P1, P2, P3, P4, P5, P6>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7> testOf(test: ThrowingConsumer7<P1, P2, P3, P4, P5, P6, P7>, vararg cases: TestCase7<P1, P2, P3, P4, P5, P6, P7>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8> testOf(test: ThrowingConsumer8<P1, P2, P3, P4, P5, P6, P7, P8>, vararg cases: TestCase8<P1, P2, P3, P4, P5, P6, P7, P8>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9> testOf(test: ThrowingConsumer9<P1, P2, P3, P4, P5, P6, P7, P8, P9>, vararg cases: TestCase9<P1, P2, P3, P4, P5, P6, P7, P8, P9>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> testOf(test: ThrowingConsumer10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>, vararg cases: TestCase10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> testOf(test: ThrowingConsumer11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>, vararg cases: TestCase11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> testOf(test: ThrowingConsumer12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>, vararg cases: TestCase12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> testOf(test: ThrowingConsumer13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>, vararg cases: TestCase13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> testOf(test: ThrowingConsumer14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>, vararg cases: TestCase14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> testOf(test: ThrowingConsumer15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>, vararg cases: TestCase15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> testOf(test: ThrowingConsumer16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>, vararg cases: TestCase16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> testOf(test: ThrowingConsumer17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>, vararg cases: TestCase17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> testOf(test: ThrowingConsumer18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>, vararg cases: TestCase18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> testOf(test: ThrowingConsumer19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>, vararg cases: TestCase19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> testOf(test: ThrowingConsumer20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>, vararg cases: TestCase20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21> testOf(test: ThrowingConsumer21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>, vararg cases: TestCase21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20, it.p21) }
/** @see TestCaseIterator */ @API(status = EXPERIMENTAL, since = "5.7") @SafeVarargs @SinceKotlin(JAVA_ONLY) fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22> testOf(test: ThrowingConsumer22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>, vararg cases: TestCase22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>) = TestCaseIterator(cases.iterator()) { test.accept(it.p1, it.p2, it.p3, it.p4, it.p5, it.p6, it.p7, it.p8, it.p9, it.p10, it.p11, it.p12, it.p13, it.p14, it.p15, it.p16, it.p17, it.p18, it.p19, it.p20, it.p21, it.p22) }
// @formatter:on
