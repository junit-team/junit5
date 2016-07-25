/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Unit tests for {@link TestPlanScannerFilter}.
 *
 * @since 1.0
 */
public class TestPlanScannerFilterTests {

	@Test
	void emptyClassAccepted() {
		assertTrue(newFilter().accept(EmptyClass.class), "accepts empty class because it is a container");
	}

	@Test
	void classWithNoTestMethodsIsAccepted() {
		assertTrue(newFilter().accept(ClassWithMethods.class),
			"accepts class with no @Test methods because it is a container");
	}

	@Test
	void classWithTestMethodsIsAccepted() {
		assertTrue(newFilter().accept(ClassWithTestMethods.class));
	}

	@Test
	void classWithNestedTestClassIsAccepted() {
		assertTrue(newFilter().accept(ClassWithNestedTestClass.class));
	}

	@Test
	void classWithDeeplyNestedTestClassIsAccepted() {
		assertTrue(newFilter().accept(ClassWithDeeplyNestedTestClass.class));
	}

	@Test
	void classWithTestFactoryIsAccepted() {
		assertTrue(newFilter().accept(ClassWithTestFactory.class));
	}

	@Test
	void classWithNestedTestFactoryIsAccepted() {
		assertTrue(newFilter().accept(ClassWithNestedTestFactory.class));
	}

	private TestPlanScannerFilter newFilter() {
		return new TestPlanScannerFilter(LauncherFactory.create());
	}

	private static class EmptyClass {
	}

	@SuppressWarnings("unused")
	private static class ClassWithMethods {

		void method1() {
		}

		void method2() {
		}
	}

	private static class ClassWithTestMethods {

		@Test
		void test1() {
		}

		@Test
		public void test2() {
		}
	}

	private static class ClassWithNestedTestClass {

		@SuppressWarnings("unused")
		void method() {
		}

		@Nested
		class TestClass {

			@Test
			void test1() {
			}
		}
	}

	private static class ClassWithDeeplyNestedTestClass {

		@Nested
		class Level1 {

			@Nested
			class Level2 {

				@Nested
				class TestClass {

					@Test
					void test1() {
					}
				}
			}
		}
	}

	private static class ClassWithTestFactory {

		@TestFactory
		Stream<DynamicTest> tests() {
			return Stream.empty();
		}
	}

	private static class ClassWithNestedTestFactory {

		@Nested
		class TestClass {

			@TestFactory
			List<DynamicTest> tests() {
				return emptyList();
			}
		}
	}

}
