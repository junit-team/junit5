/*
 * Copyright 2015-2016 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
public class IsTestMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestMethod();

	@Nested
	class IdentifiesPotentialTestMethods extends IsPotentialTestMethodTestSkeleton {

		IdentifiesPotentialTestMethods() {
			super(isTestMethod);
		}

	}

	@Nested
	class IdentifiesAnnotatedMethods {

		@Test
		void testMethodEvaluatesToTrue() throws NoSuchMethodException {
			Method testMethod = IsTestMethodTests.this.findMethod("testMethod");
			assertTrue(isTestMethod.test(testMethod));
		}

		@Test
		void nonTestMethodEvaluatesToFalse() throws NoSuchMethodException {
			Method nonTestMethod = IsTestMethodTests.this.findMethod("nonTestMethod");
			assertFalse(isTestMethod.test(nonTestMethod));
		}

	}

	private Method findMethod(String name, Class<?>... aClass) {
		return ReflectionUtils.findMethod(ClassWithTestAndNonTestMethod.class, name, aClass).get();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestAndNonTestMethod {

	@Test
	void testMethod() {
	}

	void nonTestMethod() {
	}

}

