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

@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
class TestInstancePerClassTest {
	@ParameterizedTest
	@MethodSource("parameterizedTestSourceCompanion")
	fun parameterizedTest(value: String) {
		assertEquals("companion", value)
	}

	@ParameterizedTest
	@MethodSource("parameterizedTestSourceInstance")
	fun parameterizedTestFromStatic(value: String) {
		assertEquals("instance", value)
	}

	fun parameterizedTestSourceInstance() = arrayOf(arrayOf("instance"))

	companion object {
		fun parameterizedTestSourceCompanion() = arrayOf(arrayOf("companion"))
	}
}
