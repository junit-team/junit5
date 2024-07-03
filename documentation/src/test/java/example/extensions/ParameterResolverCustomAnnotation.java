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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

// tag::user_guide[]
public class ParameterResolverCustomAnnotation {

	@Test
	@ExtendWith({FirstIntegerResolver.class, SecondIntegerResolver.class})
	void testInt(Integer i, @AnnotatedInteger Integer annotatedInteger) {
		assertEquals(1, i);
		assertEquals(2, annotatedInteger);
	}

	static class FirstIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType().equals(Integer.class)
					&& !parameterContext.isAnnotated(AnnotatedInteger.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return 1;
		}
	}

	static class SecondIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return parameterContext.isAnnotated(AnnotatedInteger.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return 2;
		}
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AnnotatedInteger {}
}
