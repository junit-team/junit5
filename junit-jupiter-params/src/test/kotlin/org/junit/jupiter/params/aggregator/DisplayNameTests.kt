/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:Suppress("unused")

package org.junit.jupiter.params.aggregator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

// https://github.com/junit-team/junit5/issues/1836
object DisplayNameTests {
    @JvmStatic
    fun data() = arrayOf(
        arrayOf("A", 1),
        arrayOf("B", 2),
        arrayOf("C", 3),
        arrayOf("", 4), // empty is okay
        arrayOf(null, 5) // null was the problem
    )

    @ParameterizedTest
    @MethodSource("data")
    fun test(char: String?, number: Int, info: TestInfo) {
        assertEquals("[$number] $char, $number", info.displayName)
    }
}
