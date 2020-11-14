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
	@MethodSource("perClassFromCompanionSource")
	fun perClassFromCompanion(value: String) {
		assertEquals("companion", value)
	}

	@ParameterizedTest
	@MethodSource("perClassFromInstanceSource")
	fun perClassFromInstance(value: String) {
		assertEquals("instance", value)
	}

	fun perClassFromInstanceSource() = arrayOf(arrayOf("instance"))

	companion object {
		fun perClassFromCompanionSource() = arrayOf(arrayOf("companion"))
	}
}
