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

import java.util.stream.Stream;

import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ClassTemplate
@ExtendWith(CalculatorClassTemplateTests.Twice.class)
class CalculatorClassTemplateTests {

	@Test
	void regularTest() {
		Calculator calculator = new Calculator();
		assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void parameterizedTest(int i) {
		Calculator calculator = new Calculator();
		assertEquals(i, calculator.add(i, 0));
	}

	static class Twice implements ClassTemplateInvocationContextProvider {

		@Override
		public boolean supportsClassTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<ClassTemplateInvocationContext> provideClassTemplateInvocationContexts(ExtensionContext context) {
			return Stream.of(new Ctx(), new Ctx());
		}

		static class Ctx implements ClassTemplateInvocationContext {
		}
	}
}
