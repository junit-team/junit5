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

import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL

/**
 * [TestCases] is an [Iterator] over one or more [DynamicTest]s. This is a
 * convenience type alias that can be used as the return type for a
 * [@TestFactory][TestFactory] that uses one of the [testOf] factory methods
 * that have many type parameters. It ensures that the signature of such
 * functions are not cluttered with a plethora of type information that is not
 * relevant.
 *
 * This is defined as an interface and not as a Kotlin `typealias` for two
 * reasons 1) we cannot use a Kotlin `typealias` in Java, and 2) we cannot
 * annotate a Kotlin `typealias` with [@API][API]. It is not defined as a Java
 * interface because this would require that Kotlin classes that implement this
 * interface have to implement [MutableIterator] instead of [Iterator] because
 * the [java.util.Iterator] has a [remove][java.util.Iterator.remove] function.
 * But, removal is not supposed to be supported.
 *
 * ## Examples
 * ```java
 * class Example {
 *     @TestFactory
 *     void TestBuilder<TestCase4<Integer, Char, String, List<Integer>>> bad() {
 *         return testOf(
 *             i, c, s, l -> //...
 *             caseOf(1, 'a', "a", Arrays.asList(10, 11, 12)),
 *             caseOf(2, 'b', "a", Arrays.asList(20, 21, 22)),
 *             caseOf(3, 'b', "a", Arrays.asList(30, 32, 33))
 *         );
 *     }
 *
 *     @TestFactory
 *     void TestCases good() {
 *         return testOf(
 *             i, c, s, l -> //...
 *             caseOf(1, 'a', "a", Arrays.asList(10, 11, 12)),
 *             caseOf(2, 'b', "a", Arrays.asList(20, 21, 22)),
 *             caseOf(3, 'b', "a", Arrays.asList(30, 32, 33))
 *         );
 *     }
 * }
 * ```
 *
 * In Kotlin the return type can usually be omitted:
 *
 * ```kotlin
 * private class Example {
 *     @TestFactory fun awesome() =
 *         testOf(
 *             case(1, 'a', "a", listOf(10, 11, 12)),
 *             case(2, 'b', "b", listOf(20, 21, 22)),
 *             case(3, 'c', "c", listOf(30, 31, 32))
 *         ) //...
 * }
 * ```
 *
 * However, there are situations where a regular function body is desired, to
 * e.g. set something up that is reused, and then [TestCases] comes into play:
 *
 * ```kotlin
 * private class Example {
 *     @TestFactory fun bad(): TestBuilder<TestCase4<Int, Char, String, List<Int>>> {
 *         val service = mock<Service>()
 *         return testOf(//...
 *     }
 *
 *     @TestFactory fun good(): TestCases {
 *         val service = mock<Service>()
 *         return testOf(//...
 *     }
 * }
 * ```
 *
 * @since 5.7
 * @see org.junit.jupiter.api.TestCaseIterator
 * @see org.junit.jupiter.api.testOf
 */
@API(status = EXPERIMENTAL, since = "5.7")
interface TestCases : Iterator<DynamicTest>
