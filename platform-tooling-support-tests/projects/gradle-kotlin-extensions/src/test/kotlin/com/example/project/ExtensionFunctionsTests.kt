/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package com.example.project

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.get
import org.junit.jupiter.params.provider.ValueSource

class ExtensionFunctionsTests {

    @Test
    fun assertions() {
        assertAll({
            assertThrows<IllegalArgumentException> {
                throw IllegalArgumentException()
            }
        })
    }

    @ParameterizedTest
    @ValueSource(ints = [1])
    fun accessor(accessor: ArgumentsAccessor) {
        val value: Int = accessor.get<Int>(0)
        assertEquals(1, value)
    }
}
