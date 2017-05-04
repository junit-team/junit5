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
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class IsTestMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestMethod();

	@Test
	void publicTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethod = this.findMethod("publicTestMethod");
		assertTrue(isTestMethod.test(publicTestMethod));
	}

	@Test
	void publicTestMethodsWithArgumentEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethodWithArgument = findMethod("publicTestMethodWithArgument", TestInfo.class);
		assertTrue(isTestMethod.test(publicTestMethodWithArgument));
	}

	@Test
	void protectedTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method protectedTestMethod = this.findMethod("protectedTestMethod");
		assertTrue(isTestMethod.test(protectedTestMethod));
	}

	@Test
	void packageVisibleTestMethodTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method packageVisibleTestMethod = this.findMethod("packageVisibleTestMethod");
		assertTrue(isTestMethod.test(packageVisibleTestMethod));
	}

	@Test
	void privateTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method privateTestMethod = this.findMethod("privateTestMethod");
		assertFalse(isTestMethod.test(privateTestMethod));
	}

	@Test
	void staticTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method staticTestMethod = this.findMethod("staticTestMethod");
		assertFalse(isTestMethod.test(staticTestMethod));
	}

	@Test
	void abstractTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method abstractTestMethod = this.findMethodOfAbstractClass("abstractTestMethod");
		assertFalse(isTestMethod.test(abstractTestMethod));
	}

	private Method findMethod(String name, Class<?>... aClass) {
		return ReflectionUtils.findMethod(ClassWithTestMethods.class, name, aClass).get();
	}

	private Method findMethodOfAbstractClass(String name) {
		return ReflectionUtils.findMethod(AbstractClassWithTestMethod.class, name).get();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestMethods {

	@Test
	void publicTestMethod() {
	}

	@Test
	void publicTestMethodWithArgument(TestInfo info) {
	}

	@Test
	protected void protectedTestMethod() {
	}

	@Test
	void packageVisibleTestMethod() {
	}

	@Test
	private void privateTestMethod() {
	}

	@Test
	static void staticTestMethod() {
	}

}

abstract class AbstractClassWithTestMethod {

	@Test
	abstract void abstractTestMethod();

}
