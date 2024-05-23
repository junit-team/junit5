/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;

// tag::user_guide[]
public class ParameterResolverDuplicateTypeInteger {

	@RegisterExtension
	static final FirstIntegerResolver firstIntegerResolver = new FirstIntegerResolver();

	@RegisterExtension
	static final SecondIntegerResolver secondIntegerResolver = new SecondIntegerResolver();

	@Test
	void testInt(int i) {
		// Test will not run due to ParameterResolutionException
		assertEquals(1, i);
	}

	static class FirstIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == int.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return 1;
		}
	}

	static class SecondIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == int.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return 2;
		}
	}
}
