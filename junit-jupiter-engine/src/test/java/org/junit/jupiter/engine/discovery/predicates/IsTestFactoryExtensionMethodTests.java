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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
public class IsTestFactoryExtensionMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestFactoryExtensionMethod();

	@Nested
	class IdentifiesPotentialTestMethods extends IsPotentialTestMethodTestSkeleton {

		IdentifiesPotentialTestMethods() {
			super(isTestMethod);
		}

	}

	@Nested
	class IdentifiesAnnotatedMethods {

		@Test
		void testFactoryMethodEvaluatesToTrue() throws NoSuchMethodException {
			Method testMethod = findMethod("testFactoryExtensionMethod");
			assertTrue(isTestMethod.test(testMethod));
		}

		@Test
		void nonTestFactoryMethodEvaluatesToFalse() throws NoSuchMethodException {
			Method nonTestMethod = findMethod("nonTestFactoryExtensionMethod");
			assertFalse(isTestMethod.test(nonTestMethod));
		}

	}

	private static Method findMethod(String name, Class<?>... aClass) {
		return ReflectionUtils.findMethod(ClassWithTestFactoryAndNonTestFactoryExtensionMethod.class, name,
			aClass).get();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestFactoryAndNonTestFactoryExtensionMethod {

	@ExtendWith(TestFactoryExtension.class)
	void testFactoryExtensionMethod() {
	}

	void nonTestFactoryExtensionMethod() {
	}

}
