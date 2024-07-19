/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

class TestClass {

	@TestAnnotation
	private final String someField;

	TestClass(String someField) {
		this.someField = someField;
	}

	@Test
	void someTestMethod() {
		assertThat(this.someField).isEqualTo("abc");
	}

}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestAnnotationExtension.class)
@interface TestAnnotation {
}

class TestAnnotationExtension implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().getType().equals(String.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return "abc";
	}

}
