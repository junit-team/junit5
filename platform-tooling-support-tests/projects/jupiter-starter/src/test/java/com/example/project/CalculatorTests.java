/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package com.example.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CalculatorTests {

	@BeforeAll
	static void printJavaVersion() {
		System.out.println("Using Java version: " + System.getProperty("java.specification.version"));
	}

	@Test
	@DisplayName("1 + 1 = 2")
	void addsTwoNumbers() {
		Calculator calculator = new Calculator();
		assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
	}

	@ParameterizedTest(name = "{0} + {1} = {2}")
	@CsvSource({ //
			"0,    1,   1", //
			"1,    2,   3", //
			"49,  51, 100", //
			"1,  100, 101" //
	})
	void add(int first, int second, int expectedResult) {
		Calculator calculator = new Calculator();
		assertEquals(expectedResult, calculator.add(first, second),
			() -> first + " + " + second + " should equal " + expectedResult);
	}

	@Nested
	@ContainerTemplate
	@ExtendWith(Twice.class)
	class NestedTests {

		@ParameterizedTest
		@ValueSource(ints = { 1, 2 })
		void test(int i) {
			assertNotEquals(0, i);
		}
	}

	static class Twice implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
				ExtensionContext context) {
			return Stream.of(new Ctx(), new Ctx());
		}

		static class Ctx implements ContainerTemplateInvocationContext {
		}
	}
}
