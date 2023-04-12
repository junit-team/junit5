package org.junit.jupiter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory


@Test
fun failure() = assertTrue(false)

@Test
fun success() = assertTrue(true)

@RepeatedTest(3)
fun repeated() = assertTrue(true)


@TestFactory
fun dynamic() = listOf(
    dynamicTest("successful top-level kotlin dynamic test") { assertTrue(true) },
    dynamicTest("failing top-level kotlin dynamic test") { assertTrue(false) }
)


