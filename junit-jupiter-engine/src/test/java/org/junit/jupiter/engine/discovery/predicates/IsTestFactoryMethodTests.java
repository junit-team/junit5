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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;

public class IsTestFactoryMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestFactoryMethod();

	@Test
	void publicTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethod = this.findMethod("factory");
		assertTrue(isTestMethod.test(publicTestMethod));
	}

	private Method findMethod(String name) {
		return ReflectionUtils.findMethod(AnotherClassWithTestFactory.class, name).get();
	}

}

class AnotherClassWithTestFactory {

	@TestFactory
	Collection<DynamicTest> factory() {
		return new ArrayList<>();
	}

}
