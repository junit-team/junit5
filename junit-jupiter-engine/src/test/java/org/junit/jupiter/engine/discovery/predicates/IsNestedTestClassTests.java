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

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class IsNestedTestClassTests {

	private final Predicate<Class<?>> isNestedTestClass = new IsNestedTestClass();

	@Test
	void nestedClassEvaluatesToTrue() {
		assertTrue(isNestedTestClass.test(ClassWithNestedInnerClasses.InnerClass.class));
	}

	@Test
	void staticNestedClassEvaluatesToFalse() {
		assertFalse(isNestedTestClass.test(ClassWithNestedInnerClasses.StaticInnerClass.class));
	}

	@Test
	void privateNestedClassEvaluatesToFalse() {
		// @formatter:off
        Class<?> privateInnerClass = Arrays.stream(ClassWithInnerClasses.class.getDeclaredClasses())
                .filter(aClass -> aClass.getSimpleName().equals("PrivateInnerClass"))
                .findFirst()
                .get();
		// @formatter:on

		assertFalse(isNestedTestClass.test(privateInnerClass));
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithNestedInnerClasses {

	@Nested
	class InnerClass {
	}

	@Nested
	private class PrivateInnerClass {
	}

	@Nested
	static class StaticInnerClass {
	}

}
