/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * @since 5.0
 */
class IsScannableTestClassTests {

	private final Predicate<Class<?>> isScannableTestClass = new IsScannableTestClass();

	@Test
	void privateStandardTestClassEvaluatesToFalse() {
		assertFalse(isScannableTestClass.test(PrivateClassWithTestCases.class));
	}

	@Test
	void privateClassWithTestFactoryEvaluatesToFalse() {
		assertFalse(isScannableTestClass.test(PrivateClassWithTestFactory.class));
	}

	@Test
	void privateClassWithNestedTestCasesEvaluatesToFalse() {
		assertFalse(isScannableTestClass.test(PrivateClassWithNestedTestCases.class));
	}

	@Test
	void standardTestClassEvaluatesToTrue() {
		assertTrue(isScannableTestClass.test(ClassWithTestCases.class));
	}

	@Test
	void classWithTestFactoryEvaluatesToTrue() {
		assertTrue(isScannableTestClass.test(ClassWithTestFactory.class));
	}

	@Test
	void classWithNestedTestCasesEvaluatesToTrue() {
		assertTrue(isScannableTestClass.test(ClassWithNestedTestCases.class));
	}

	private class PrivateClassWithTestFactory {

		@TestFactory
		Collection<DynamicTest> factory() {
			return new ArrayList<>();
		}

	}

	private class PrivateClassWithTestCases {

		@Test
		void first() {
		}

		@Test
		void second() {
		}

	}

	private class PrivateClassWithNestedTestCases {

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

}
