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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodSourceWithoutKotlinTest {
	@ParameterizedTest
	@MethodSource("parameterizedTestSource")
	void parameterizedTest(String value) {
		assertEquals("no-kotlin", value);
	}

	private static Object parameterizedTestSource() {
		return new Object[] {new Object[] {"no-kotlin"}};
	}
}
