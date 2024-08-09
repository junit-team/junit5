/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link IsTestMethod}.
 *
 * @since 5.0
 */
class IsTestMethodTests {

	private static final Predicate<Method> isTestMethod = new IsTestMethod();

	@Test
	void publicTestMethod() {
		Method method = method("publicTestMethod");
		// Ensure that somebody doesn't accidentally delete the public modifier again.
		assertTrue(ReflectionUtils.isPublic(method));
		assertThat(isTestMethod).accepts(method);
	}

	@Test
	void publicTestMethodWithArgument() {
		Method method = method("publicTestMethodWithArgument", TestInfo.class);
		// Ensure that somebody doesn't accidentally delete the public modifier again.
		assertTrue(ReflectionUtils.isPublic(method));
		assertThat(isTestMethod).accepts(method);
	}

	@Test
	void protectedTestMethod() {
		assertThat(isTestMethod).accepts(method("protectedTestMethod"));
	}

	@Test
	void packageVisibleTestMethod() {
		assertThat(isTestMethod).accepts(method("packageVisibleTestMethod"));
	}

	@Test
	void bogusAbstractTestMethod() {
		assertThat(isTestMethod).rejects(abstractMethod("bogusAbstractTestMethod"));
	}

	@Test
	void bogusStaticTestMethod() {
		assertThat(isTestMethod).rejects(method("bogusStaticTestMethod"));
	}

	@Test
	void bogusPrivateTestMethod() {
		assertThat(isTestMethod).rejects(method("bogusPrivateTestMethod"));
	}

	@Test
	void bogusTestMethodReturningObject() {
		assertThat(isTestMethod).rejects(method("bogusTestMethodReturningObject"));
	}

	@Test
	void bogusTestMethodReturningVoidReference() {
		assertThat(isTestMethod).rejects(method("bogusTestMethodReturningVoidReference"));
	}

	@Test
	void bogusTestMethodReturningPrimitive() {
		assertThat(isTestMethod).rejects(method("bogusTestMethodReturningPrimitive"));
	}

	private static Method method(String name, Class<?>... parameterTypes) {
		return ReflectionUtils.findMethod(ClassWithTestMethods.class, name, parameterTypes).get();
	}

	private Method abstractMethod(String name) {
		return ReflectionUtils.findMethod(AbstractClassWithAbstractTestMethod.class, name).get();
	}

	private static abstract class AbstractClassWithAbstractTestMethod {

		@Test
		abstract void bogusAbstractTestMethod();

	}

	private static class ClassWithTestMethods {

		@Test
		static void bogusStaticTestMethod() {
		}

		@Test
		private void bogusPrivateTestMethod() {
		}

		@Test
		String bogusTestMethodReturningObject() {
			return "";
		}

		@Test
		Void bogusTestMethodReturningVoidReference() {
			return null;
		}

		@Test
		int bogusTestMethodReturningPrimitive() {
			return 0;
		}

		@Test
		public void publicTestMethod() {
		}

		@Test
		public void publicTestMethodWithArgument(TestInfo info) {
		}

		@Test
		protected void protectedTestMethod() {
		}

		@Test
		void packageVisibleTestMethod() {
		}

	}

}
