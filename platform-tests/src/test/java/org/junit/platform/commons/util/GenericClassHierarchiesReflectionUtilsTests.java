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

import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GenericClassHierarchiesReflectionUtilsTests {
	@Test
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

	interface InterfaceDouble {
		default void foo(Double parameter) {
		}
	}

	interface InterfaceGenericNumber<T extends Number> {
		default void foo(T parameter) {
		}
	}
}
