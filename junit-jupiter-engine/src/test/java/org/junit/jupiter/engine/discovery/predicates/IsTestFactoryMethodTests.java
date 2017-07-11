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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;

class IsTestFactoryMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestFactoryMethod();

	@Test
	void factoryMethodReturningCollectionEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethod = this.findMethod("factory");
		assertTrue(isTestMethod.test(publicTestMethod));
	}

	@Test
	void factoryMethodReturningVoidEvaluatesToFalse() throws NoSuchMethodException {
		Method publicTestMethod = this.findMethod("badFactory");
		assertFalse(isTestMethod.test(publicTestMethod));
	}

	private Method findMethod(String name) {
		return ReflectionUtils.findMethod(AnotherClassWithTestFactory.class, name).get();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class AnotherClassWithTestFactory {

	@TestFactory
	Collection<DynamicTest> factory() {
		return new ArrayList<>();
	}

	@TestFactory
	void badFactory() {
	}

}
