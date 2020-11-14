/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.provider

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest

class MethodSourceFromInheritedCompanionTest : BaseTest() {
    @ParameterizedTest
    @MethodSource("childTestSource")
    fun childTest(value: String) {
        assertEquals("child", value)
    }

    @ParameterizedTest
    @MethodSource("parentTestSource")
    fun parentTest(value: String) {
        assertEquals("parent", value)
    }

    @ParameterizedTest
    @MethodSource("deepParentTestSource")
    fun deepParentTest(value: String) {
        assertEquals("deep", value)
    }

    @ParameterizedTest
    @MethodSource("org.junit.jupiter.params.provider.BaseTest.Companion#parentTestSource")
    fun fullyQualifiedParentTest1(value: String) {
        assertEquals("parent", value)
    }

    @ParameterizedTest
    @MethodSource("org.junit.jupiter.params.provider.BaseTest#parentTestSource")
    fun fullyQualifiedParentTest2(value: String) {
        assertEquals("parent", value)
    }

    @ParameterizedTest
    @MethodSource("org.junit.jupiter.params.provider.DeepBaseTest#parentTestSource")
    fun fullyQualifiedParentTest3(value: String) {
        assertEquals("ignored-by-implicit-lookup", value)
    }

    @ParameterizedTest
    @MethodSource("org.junit.jupiter.params.provider.DeepBaseTest#deepParentTestSource")
    fun fullyQualifiedParentTest4(value: String) {
        assertEquals("deep", value)
    }

    companion object {
        fun childTestSource() = arrayOf(arrayOf("child"))
    }
}

open class BaseTest : DeepBaseTest() {
    companion object {
        fun parentTestSource() = arrayOf(arrayOf("parent"))
    }
}

open class DeepBaseTest {
    companion object {
        fun parentTestSource() = arrayOf(arrayOf("ignored-by-implicit-lookup"))

        fun deepParentTestSource() = arrayOf(arrayOf("deep"))
    }
}
