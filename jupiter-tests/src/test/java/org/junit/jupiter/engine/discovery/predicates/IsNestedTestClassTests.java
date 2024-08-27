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

import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class IsNestedTestClassTests {

	private final Predicate<Class<?>> isNestedTestClass = new IsNestedTestClass();

	@Test
	void innerClassEvaluatesToTrue() {
		assertThat(isNestedTestClass).accepts(NestedClassesTestCase.InnerClass.class);
	}

	@Test
	void staticNestedClassEvaluatesToFalse() {
		assertThat(isNestedTestClass).rejects(NestedClassesTestCase.StaticNestedClass.class);
	}

	@Test
	void privateNestedClassEvaluatesToFalse() {
		assertThat(isNestedTestClass).rejects(NestedClassesTestCase.PrivateInnerClass.class);
	}

	private static class NestedClassesTestCase {

		@Nested
		class InnerClass {
		}

		@Nested
		static class StaticNestedClass {
		}

		@Nested
		private class PrivateInnerClass {
		}

	}

}
