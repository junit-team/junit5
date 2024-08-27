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

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class IsInnerClassTests {

	private final Predicate<Class<?>> isInnerClass = new IsInnerClass();

	@Test
	void innerClassEvaluatesToTrue() {
		assertThat(isInnerClass).accepts(InnerClassesTestCase.InnerClass.class);
	}

	@Test
	void staticNestedClassEvaluatesToFalse() {
		assertThat(isInnerClass).rejects(InnerClassesTestCase.StaticNestedClass.class);
	}

	@Test
	void privateInnerClassEvaluatesToFalse() {
		assertThat(isInnerClass).rejects(InnerClassesTestCase.PrivateInnerClass.class);
	}

	private static class InnerClassesTestCase {

		class InnerClass {
		}

		static class StaticNestedClass {
		}

		private class PrivateInnerClass {
		}

	}

}
