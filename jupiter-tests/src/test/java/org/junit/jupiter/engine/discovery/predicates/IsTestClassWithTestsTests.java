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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;

/**
 * Unit tests for {@link IsTestClassWithTests}.
 *
 * @since 5.0
 */
class IsTestClassWithTestsTests {

	private final Predicate<Class<?>> isTestClassWithTests = new IsTestClassWithTests();

	@Test
	void classWithTestMethodEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestMethod.class));
	}

	@Test
	void classWithTestFactoryEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestFactory.class));
	}

	@Test
	void classWithTestTemplateEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestTemplate.class));
	}

	@Test
	void classWithNestedTestClassEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithNestedTestClass.class));
	}

	@Test
	void staticTestClassEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(StaticTestCase.class));
	}

	// -------------------------------------------------------------------------

	@Test
	void privateClassWithTestMethodEvaluatesToFalse() {
		assertFalse(isTestClassWithTests.test(PrivateClassWithTestMethod.class));
	}

	@Test
	void privateClassWithTestFactoryEvaluatesToFalse() {
		assertFalse(isTestClassWithTests.test(PrivateClassWithTestFactory.class));
	}

	@Test
	void privateClassWithTestTemplateEvaluatesToFalse() {
		assertFalse(isTestClassWithTests.test(PrivateClassWithTestTemplate.class));
	}

	@Test
	void privateClassWithNestedTestCasesEvaluatesToFalse() {
		assertFalse(isTestClassWithTests.test(PrivateClassWithNestedTestClass.class));
	}

	@Test
	void privateStaticTestClassEvaluatesToFalse() {
		assertFalse(isTestClassWithTests.test(PrivateStaticTestCase.class));
	}

	/**
	 * @see https://github.com/junit-team/junit5/issues/2249
	 */
	@Test
	void recursiveHierarchies() {
		assertTrue(isTestClassWithTests.test(OuterClass.class));
		assertFalse(isTestClassWithTests.test(OuterClass.RecursiveInnerClass.class));
	}

	// -------------------------------------------------------------------------

	private class PrivateClassWithTestMethod {

		@Test
		void test() {
		}

	}

	private class PrivateClassWithTestFactory {

		@TestFactory
		Collection<DynamicTest> factory() {
			return new ArrayList<>();
		}

	}

	private class PrivateClassWithTestTemplate {

		@TestTemplate
		void template(int a) {
		}

	}

	private class PrivateClassWithNestedTestClass {

		@Nested
		class InnerClass {

			@Test
			void first() {
			}

			@Test
			void second() {
			}

		}
	}

	// -------------------------------------------------------------------------

	static class StaticTestCase {

		@Test
		void test() {
		}
	}

	private static class PrivateStaticTestCase {

		@Test
		void test() {
		}
	}

	static class OuterClass {

		@Nested
		class InnerClass {

			@Test
			void test() {
			}
		}

		// Intentionally commented out so that RecursiveInnerClass is NOT a candidate test class
		// @Nested
		class RecursiveInnerClass extends OuterClass {
		}
	}

}

// -----------------------------------------------------------------------------

class ClassWithTestMethod {

	@Test
	void test() {
	}

}

class ClassWithTestFactory {

	@TestFactory
	Collection<DynamicTest> factory() {
		return new ArrayList<>();
	}

}

class ClassWithTestTemplate {

	@TestTemplate
	void template(int a) {
	}

}

class ClassWithNestedTestClass {

	@Nested
	class InnerClass {

		@Test
		void first() {
		}

		@Test
		void second() {
		}

	}
}
