/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

class MethodSourceTests {

	@Test
	void instantiatingWithNullNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource(null, null));
	}

	@Test
	void instantiatingWithEmptyNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("", ""));
	}

	@Test
	void instantiatingWithBlankNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("  ", "  "));
	}

	@Test
	void instantiationWithNullMethodShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource(null));
	}

	@Test
	void twoEqualMethodsShouldHaveEqualMethodSourceObjects() {
		assertEquals(new MethodSource("TestClass1", "testMethod1"), new MethodSource("TestClass1", "testMethod1"));
	}

	@Test
	void twoUnequalMethodsShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(new MethodSource("TestClass1", "testMethod1"), new MethodSource("TestClass2", "testMethod1"));
	}

	@Test
	void twoUnequalMethodsInTheSameClassShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(new MethodSource("TestClass1", "testMethod1"), new MethodSource("TestClass1", "testMethod2"));
	}

	@Test
	void twoEqualMethodSourceObjectsShouldHaveEqualHashCode() {
		assertEquals(new MethodSource("TestClass1", "testMethod1").hashCode(),
			new MethodSource("TestClass1", "testMethod1").hashCode());
	}

	@Test
	void twoEqualMethodsWithEqualParametersShouldHaveEqualMethodSourceObjects() {
		assertEquals(new MethodSource("TestClass1", "testMethod1", "int, String"),
			new MethodSource("TestClass1", "testMethod1", "int, String"));
	}

	@Test
	void twoUnequalMethodsWithEqualParametersShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(new MethodSource("TestClass1", "testMethod1", "int, String"),
			new MethodSource("TestClass1", "testMethod2", "int, String"));
	}

	@Test
	void twoEqualMethodsWithUnequalParametersShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(new MethodSource("TestClass1", "testMethod1", "int, String"),
			new MethodSource("TestClass1", "testMethod1", "float, int, String"));
	}

	@Test
	void twoEqualMethodsWithEqualParametersShouldHaveEqualMethodSourceHashCodes() {
		assertEquals(new MethodSource("TestClass1", "testMethod1", "int, String").hashCode(),
			new MethodSource("TestClass1", "testMethod1", "int, String").hashCode());
	}

	@Test
	void twoEqualMethodsWithUnequalParametersShouldHaveUnequalMethodSourceHashCodes() {
		assertNotEquals(new MethodSource("TestClass1", "testMethod1", "int, String").hashCode(),
			new MethodSource("TestClass1", "testMethod1", "float, int, String"));
	}

	@Test
	void aReflectedMethodsClassNameShouldBeConsistent() throws Exception {
		Class<?> c = String.class;
		Method m = c.getDeclaredMethod("valueOf", int.class);

		assertEquals("java.lang.String", new MethodSource(m).getClassName());
	}

	@Test
	void aReflectedMethodsMethodNameShouldBeConsistent() throws Exception {
		Class<?> c = String.class;
		Method m = c.getDeclaredMethod("valueOf", int.class);

		assertEquals("valueOf", new MethodSource(m).getMethodName());
	}

	@Test
	void aReflectedMethodsParameterTypesShouldBeConsistent() throws Exception {
		Class<?> c = String.class;
		Method m = c.getDeclaredMethod("valueOf", float.class);

		assertEquals("float", new MethodSource(m).getMethodParameterTypes());
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceObjects() throws Exception {
		Class<?> c1 = String.class;
		Method m1 = c1.getDeclaredMethod("valueOf", int.class);

		Class<?> c2 = String.class;
		Method m2 = c2.getDeclaredMethod("valueOf", int.class);

		assertEquals(new MethodSource(m1), new MethodSource(m2));
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceHashCodes() throws Exception {
		Class<?> c1 = String.class;
		Method m1 = c1.getDeclaredMethod("valueOf", int.class);

		Class<?> c2 = String.class;
		Method m2 = c2.getDeclaredMethod("valueOf", int.class);

		assertEquals(new MethodSource(m1).hashCode(), new MethodSource(m2).hashCode());
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceObjects() throws Exception {
		Class<?> c1 = String.class;
		Method m1 = c1.getDeclaredMethod("valueOf", int.class);

		Class<?> c2 = Byte.class;
		Method m2 = c2.getDeclaredMethod("byteValue");

		assertNotEquals(new MethodSource(m1), new MethodSource(m2));
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceHashCodes() throws Exception {
		Class<?> c1 = String.class;
		Method m1 = c1.getDeclaredMethod("valueOf", int.class);

		Class<?> c2 = Byte.class;
		Method m2 = c2.getDeclaredMethod("byteValue");

		assertNotEquals(new MethodSource(m1).hashCode(), new MethodSource(m2).hashCode());
	}
}
