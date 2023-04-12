/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.engine.kotlin

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

@Disabled
@Test
fun failure() = assertTrue(false)

@Test
fun success() = assertTrue(true)

@RepeatedTest(3)
fun repeated() = assertTrue(true)

@TestFactory
fun dynamic() = listOf(
    dynamicTest("successful top-level kotlin dynamic test") { assertTrue(true) }
//    dynamicTest("failing top-level kotlin dynamic test") { assertTrue(false) }
)
