/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.aggregator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Month
import java.util.*

/**
 * Tests for ParameterizedTest kotlin compatibility
 */
object KotlinParameterizedTests {

    @ParameterizedTest
    @MethodSource("dataProvidedByKotlinSequence")
    fun `a method source can be supplied by a Sequence returning method`(value: Int, month: Month) {
        assertEquals(value, month.value)
    }

    @JvmStatic
    private fun dataProvidedByKotlinSequence() = sequenceOf(
        arrayOf(1, Month.JANUARY),
        arrayOf(3, Month.MARCH),
        arrayOf(8, Month.AUGUST),
        arrayOf(5, Month.MAY),
        arrayOf(12, Month.DECEMBER)
    )
}
