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

class MethodSourceFromOtherJvmStaticCompanionTest {
    @ParameterizedTest
    @MethodSource("org.junit.jupiter.params.provider.OtherClassJvmStatic#parameterizedTestSource")
    fun parameterizedTest(value: String) {
        assertEquals("foo", value)
    }
}

class OtherClassJvmStatic {
    companion object {
        @JvmStatic
        fun parameterizedTestSource() = arrayOf(arrayOf("foo"))
    }
}
