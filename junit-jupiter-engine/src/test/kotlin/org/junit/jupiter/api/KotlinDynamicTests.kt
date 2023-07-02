/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.MathContext
import java.math.BigInteger as BigInt
import java.math.RoundingMode as Rounding

/**
 * Unit tests for JUnit Jupiter [TestFactory] use in kotlin classes.
 *
 * @since 5.10
 */
class KotlinDynamicTests {

    @Nested
    inner class SequenceReturningTestFactoryTests {

        @TestFactory
        fun `Dynamic tests returned as Kotlin sequence`() = generateSequence(0) { it + 2 }
            .map { dynamicTest("$it should be even") { assertTrue(it % 2 == 0) } }
            .take(10)

        @TestFactory
        fun `Is anagram tests`(): Sequence<DynamicTest> {
            infix fun CharSequence.isAngramOf(other: CharSequence) = groupBy { it } == other.groupBy { it }

            infix fun CharSequence.`should be an anagram of`(other: CharSequence) =
                dynamicTest("'$this' should be an anagram of '$other'") { assertTrue(this isAngramOf other) }

            infix fun CharSequence.`should not be an anagram of`(other: CharSequence) =
                dynamicTest("'$this' should not be an anagram of '$other'") { assertFalse(this isAngramOf other) }

            return sequenceOf(
                "a gentleman" `should be an anagram of` "elegant man",
                "laptop machines" `should be an anagram of` "apple macintosh",
                "salvador dali" `should be an anagram of` "avida dollars",
                "a gentleman" `should not be an anagram of` "spider man",
                "laptop computers" `should not be an anagram of` "apple macintosh",
                "salvador dali" `should not be an anagram of` "picasso"
            )
        }

        @TestFactory
        fun `Consecutive fibonacci nr ratios, should converge to golden ratio as n increases`(): Sequence<DynamicTest> {
            val scale = 5
            val goldenRatio = (ONE + 5.toBigDecimal().sqrt(MathContext(scale + 10, Rounding.HALF_UP)))
                .divide(2.toBigDecimal(), scale, Rounding.HALF_UP)

            fun shouldApproximateGoldenRatio(cur: BigDecimal, next: BigDecimal) =
                next.divide(cur, scale, Rounding.HALF_UP).let {
                    dynamicTest("$cur / $next = $it should approximate the golden ratio in $scale decimals") {
                        assertEquals(goldenRatio, it)
                    }
                }
            return generateSequence(BigInt.ONE to BigInt.ONE) { (cur, next) -> next to cur + next }
                .map { (cur) -> cur.toBigDecimal() }
                .zipWithNext(::shouldApproximateGoldenRatio)
                .drop(14)
                .take(10)
        }
    }
}
