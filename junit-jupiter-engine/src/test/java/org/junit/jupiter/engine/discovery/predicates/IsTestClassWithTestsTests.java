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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;

class IsTestClassWithTestsTests {

	private final Predicate<Class<?>> isTestClassWithTests = new IsTestClassWithTests();

	@Test
	void standardTestClassEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestCases.class));
	}

	@Test
	void classWithTestFactoryEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestFactory.class));
	}

	@Test
	void classWithNestedTestCasesEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithNestedTestCases.class));
	}

	@Test
	void classWithTestTemplateEvaluatesToTrue() {
		assertTrue(isTestClassWithTests.test(ClassWithTestTemplate.class));
	}
}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestFactory {

	@TestFactory
	Collection<DynamicTest> factory() {
		return new ArrayList<>();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestCases {

	@Test
	void first() {
	}

	@Test
	void second() {
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithNestedTestCases {

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

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestTemplate {

	@TestTemplate
	void first(int a) {
	}

}
