/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GenericClassHierarchiesReflectionUtilsTests {
	@Test
	@Disabled("Describes a new case that does not yet yield the expected result.")
	void findsMethodsIndependentlyFromOrderOfImplementationsOfInterfaces() {

		class AB implements InterfaceDouble, InterfaceGenericNumber {
		}

		class BA implements InterfaceGenericNumber, InterfaceDouble {
		}

		var methodAB = findMethod(AB.class, "foo", Double.class).orElseThrow();
		var methodBA = findMethod(BA.class, "foo", Double.class).orElseThrow();

		Assertions.assertEquals(methodAB, methodBA);
	}

	@Test
	void findMoreSpecificMethodFromAbstractImplementationOverDefaultInterfaceMethod() {
		class A implements InterfaceGenericNumber<Long> {
			@Override
			public void foo(Long parameter) {

			}
		}

		Method foo = findMethod(A.class, "foo", Long.class).orElseThrow();

		Assertions.assertEquals(A.class, foo.getDeclaringClass());
	}

	@Test
	@Disabled("Describes a new case that does not yet yield the expected result.")
	void findMoreSpecificMethodFromOverriddenImplementationOfGenericInterfaceMethod() {
		class A implements InterfaceGenericNumber<Number> {
			@Override
			public void foo(Number parameter) {
			}
		}

		Method foo = findMethod(A.class, "foo", Long.class).orElseThrow();

		Assertions.assertEquals(A.class, foo.getDeclaringClass());
	}

	@Test
	@Disabled("Describes a new case that does not yet yield the expected result.")
	void findMoreSpecificMethodFromImplementationOverDefaultInterfaceMethodAndGenericClassExtension() {

		class AParent {
			public void foo(Number parameter) {
			}
		}

		class A extends AParent implements InterfaceGenericNumber<Number> {
			@Override
			public void foo(Number parameter) {

			}
		}

		Method foo = findMethod(A.class, "foo", Long.class).orElseThrow();

		Assertions.assertEquals(A.class, foo.getDeclaringClass());
	}

	@Test
	@Disabled("Expected behaviour is not clear yet.")
	void unclearPrecedenceOfImplementationsInParentClassAndInterfaceDefault() {

		class AParent {
			public void foo(Number parameter) {
			}
		}

		class A extends AParent implements InterfaceGenericNumber<Number> {
		}

		Method foo = findMethod(A.class, "foo", Long.class).orElseThrow();

		// ????????
		Assertions.assertEquals(A.class, foo.getDeclaringClass());
		Assertions.assertEquals(AParent.class, foo.getDeclaringClass());
		Assertions.assertEquals(InterfaceGenericNumber.class, foo.getDeclaringClass());
	}

	@Test
	@Disabled("Describes cases where current implementation returns unexpected value")
	public void findMethodWithMostSpecificParameterTypeInHierarchy() {
		// Searched Parameter Type is more specific
		assertSpecificFooMethodFound(ClassImplementingInterfaceWithInvertedHirarchy.class,
			InterfaceWithGenericNumberParameter.class, Double.class);
		assertSpecificFooMethodFound(ClassImplementingGenericInterfaceWithMoreSpecificMethod.class,
			ClassImplementingGenericInterfaceWithMoreSpecificMethod.class, Double.class);
		assertSpecificFooMethodFound(ClassImplementingGenericAndMoreSpecificInterface.class,
			InterfaceWithGenericNumberParameter.class, Double.class);
		assertSpecificFooMethodFound(ClassOverridingDefaultMethodAndImplementingMoreSpecificInterface.class,
			ClassOverridingDefaultMethodAndImplementingMoreSpecificInterface.class, Double.class);

		// Exact Type Match
		assertSpecificFooMethodFound(ClassImplementingGenericInterfaceWithMoreSpecificMethod.class,
			ClassImplementingGenericInterfaceWithMoreSpecificMethod.class, Number.class);
	}

	private void assertSpecificFooMethodFound(Class<?> classToSearchIn, Class<?> classWithMostSpecificMethod,
			Class<?> parameterType) {
		Method foo = findMethod(classToSearchIn, "foo", parameterType).orElseThrow();
		assertDeclaringClass(foo, classWithMostSpecificMethod);
	}

	private void assertDeclaringClass(Method method, Class<?> expectedClass) {
		assertThat(method.getDeclaringClass()).isEqualTo(expectedClass);
	}

	interface InterfaceDouble {
		default void foo(Double parameter) {
		}
	}

	interface InterfaceGenericNumber<T extends Number> {
		default void foo(T parameter) {
		}
	}

	public interface InterfaceWithGenericObjectParameter {

		default <T extends Object> void foo(T a) {
		}
	}

	public interface InterfaceWithGenericNumberParameter {

		default <T extends Number> void foo(T a) {
		}
	}

	public interface InterfaceExtendingNumberInterfaceWithGenericObjectMethod
			extends InterfaceWithGenericNumberParameter {

		default <T extends Object> void foo(T a) {
		}
	}

	public class ClassImplementingGenericInterfaceWithMoreSpecificMethod
			implements InterfaceWithGenericObjectParameter {

		public void foo(Number a) {
		}
	}

	public class ClassImplementingGenericAndMoreSpecificInterface
			implements InterfaceWithGenericObjectParameter, InterfaceWithGenericNumberParameter {
	}

	public class ClassOverridingDefaultMethodAndImplementingMoreSpecificInterface
			implements InterfaceWithGenericObjectParameter, InterfaceWithGenericNumberParameter {

		@Override
		public <T> void foo(T a) {
		}
	}

	public class ClassImplementingInterfaceWithInvertedHirarchy
			implements InterfaceExtendingNumberInterfaceWithGenericObjectMethod {
	}
}
