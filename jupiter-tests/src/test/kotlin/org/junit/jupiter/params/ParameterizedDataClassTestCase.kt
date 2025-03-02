/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.provider.ValueSource

@ParameterizedClass
@ValueSource(ints = [-1, 1])
data class ParameterizedDataClassTestCase(
    val value: Int,
    val testInfo: TestInfo
) {
    @Test
    fun test1() {
        assertEquals("test1()", testInfo.displayName)
        assertTrue(value < 0, "negative")
    }

    @Test
    fun test2() {
        assertEquals("test2()", testInfo.displayName)
        assertTrue(value < 0, "negative")
    }
}
