/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package com.example.project;

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MethodSourceFromCompanionTest {
	@ParameterizedTest
	@MethodSource("parameterizedTestSource")
	fun parameterizedTest(value: String) {
		assertEquals("companion", value)
	}

	@ParameterizedTest
	@MethodSource("com.example.project.Foo#parameterizedTestSource")
	fun parameterizedTestFromOtherCompanion(value: String) {
		assertEquals("other-companion", value)
	}

	@ParameterizedTest
	@MethodSource("parameterizedTestSourceStatic")
	fun parameterizedTestFromStatic(value: String) {
		assertEquals("jvmstatic", value)
	}

	companion object {
		fun parameterizedTestSource() = arrayOf(arrayOf("companion"))

		@JvmStatic
		fun parameterizedTestSourceStatic() = arrayOf(arrayOf("jvmstatic"))
	}
}


class Foo {
	companion object {
		fun parameterizedTestSource() = arrayOf(arrayOf("other-companion"))
	}
}
