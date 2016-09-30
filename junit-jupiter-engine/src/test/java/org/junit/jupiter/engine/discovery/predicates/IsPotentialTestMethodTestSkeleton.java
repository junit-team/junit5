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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract superclass to tests that verify that test methods are correctly identified
 * by dedicated predicates.
 *
 * @since 5.0
 */
abstract class IsPotentialTestMethodTestSkeleton {

	private final Predicate<Method> isPotentialTestMethod;

	IsPotentialTestMethodTestSkeleton(Predicate<Method> isPotentialTestMethod) {
		this.isPotentialTestMethod = isPotentialTestMethod;
	}

	@Test
	void publicMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method publicMethod = this.findMethod("publicMethod");
		assertTrue(isPotentialTestMethod.test(publicMethod));
	}

	@Test
	void publicMethodsWithArgumentEvaluatesToTrue() throws NoSuchMethodException {
		Method publicMethodWithArgument = findMethod("publicMethodWithArgument", TestInfo.class);
		assertTrue(isPotentialTestMethod.test(publicMethodWithArgument));
	}

	@Test
	void protectedMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method protectedMethod = this.findMethod("protectedMethod");
		assertTrue(isPotentialTestMethod.test(protectedMethod));
	}

	@Test
	void packageVisibleMethodMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method packageVisibleMethod = this.findMethod("packageVisibleMethod");
		assertTrue(isPotentialTestMethod.test(packageVisibleMethod));
	}

	@Test
	void privateMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method privateMethod = this.findMethod("privateMethod");
		assertFalse(isPotentialTestMethod.test(privateMethod));
	}

	@Test
	void staticMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method staticMethod = this.findMethod("staticMethod");
		assertFalse(isPotentialTestMethod.test(staticMethod));
	}

	@Test
	void abstractMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method abstractMethod = this.findMethodOfAbstractClass("abstractMethod");
		assertFalse(isPotentialTestMethod.test(abstractMethod));
	}

	private Method findMethod(String name, Class<?>... aClass) {
		return ReflectionUtils.findMethod(ClassWithMethods.class, name, aClass).get();
	}

	private Method findMethodOfAbstractClass(String name) {
		return ReflectionUtils.findMethod(AbstractClassWithMethod.class, name).get();
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithMethods {

	@Testlike
	public void publicMethod() {
	}

	@Testlike
	public void publicMethodWithArgument(TestInfo info) {
	}

	@Testlike
	protected void protectedMethod() {
	}

	@Testlike
	void packageVisibleMethod() {
	}

	@Testlike
	private void privateMethod() {
	}

	@Testlike
	static void staticMethod() {
	}

}

abstract class AbstractClassWithMethod {

	@Testlike
	abstract void abstractMethod();

}

/**
 * An annotation that is meta-annotated with all "test-like" annotations.
 *
 * <p>As the test class is reused with different test-identifying predicates,
 * all the annotations those predicated might be looking for must be present.
 * Otherwise the predicates fail the method based on a missing annotation.</p>
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Test
@TestFactory
@ExtendWith(TestFactoryExtension.class)
@interface Testlike {

	class MockTestFactoryExtension implements TestFactoryExtension {

		@Override
		public Stream<DynamicTest> createForContainer(
				ContainerExtensionContext context) {
			return Stream.empty();
		}

		@Override
		public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
			return Stream.empty();
		}
	}

}

