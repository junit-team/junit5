/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package example

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

// tag::test_instance_per_class[]
@TestInstance(PER_CLASS)
class TestInstancePerClassTest {
    @ParameterizedTest
    @MethodSource("fromInstanceMethodSource")
    fun fromInstanceMethod(value: String) {
        Assertions.assertEquals("instance", value)
    }

    fun fromInstanceMethodSource() = arrayOf(arrayOf("instance"))
}
// end::test_instance_per_class[]

// tag::jvm_static[]
class MethodSourceFromJvmStaticTest {
    @ParameterizedTest
    @MethodSource("jvmStaticSource")
    fun fromJvmStatic(value: String) {
        Assertions.assertEquals("jvm-static", value)
    }

    companion object {
        @JvmStatic
        fun jvmStaticSource() = arrayOf(arrayOf("jvm-static"))
    }
}
// end::jvm_static[]

// tag::companion[]
class MethodSourceFromCompanionTest {
    @ParameterizedTest
    @MethodSource("companionSource")
    fun fromCompanion(value: String) {
        Assertions.assertEquals("companion", value)
    }

    companion object {
        @JvmStatic
        fun companionSource() = arrayOf(arrayOf("companion"))
    }
}
// end::companion[]
