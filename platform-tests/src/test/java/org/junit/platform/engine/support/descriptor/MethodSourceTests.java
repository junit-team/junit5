/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link MethodSource}.
 *
 * @since 1.0
 */
class MethodSourceTests {

	@Test
	void instantiatingWithNullNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from("foo", null));
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from(null, "foo"));
	}

	@Test
	void instantiatingWithEmptyNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from("foo", ""));
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from("", "foo"));
	}

	@Test
	void instantiatingWithBlankNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from("foo", "  "));
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from("  ", "foo"));
	}

	@Test
	void instantiationWithNullMethodShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> MethodSource.from(null));
	}

	@Test
	void twoEqualMethodsShouldHaveEqualMethodSourceObjects() {
		assertEquals(MethodSource.from("TestClass1", "testMethod1"), MethodSource.from("TestClass1", "testMethod1"));
	}

	@Test
	void twoUnequalMethodsShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(MethodSource.from("TestClass1", "testMethod1"), MethodSource.from("TestClass2", "testMethod1"));
	}

	@Test
	void twoUnequalMethodsInTheSameClassShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(MethodSource.from("TestClass1", "testMethod1"), MethodSource.from("TestClass1", "testMethod2"));
	}

	@Test
	void twoEqualMethodSourceObjectsShouldHaveEqualHashCodes() {
		assertEquals(MethodSource.from("TestClass1", "testMethod1").hashCode(),
			MethodSource.from("TestClass1", "testMethod1").hashCode());
	}

	@Test
	void twoEqualMethodsWithEqualParametersShouldHaveEqualMethodSourceObjects() {
		assertEquals(MethodSource.from("TestClass1", "testMethod1", "int, String"),
			MethodSource.from("TestClass1", "testMethod1", "int, String"));
	}

	@Test
	void twoUnequalMethodsWithEqualParametersShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(MethodSource.from("TestClass1", "testMethod1", "int, String"),
			MethodSource.from("TestClass1", "testMethod2", "int, String"));
	}

	@Test
	void twoEqualMethodsWithUnequalParametersShouldHaveUnequalMethodSourceObjects() {
		assertNotEquals(MethodSource.from("TestClass1", "testMethod1", "int, String"),
			MethodSource.from("TestClass1", "testMethod1", "float, int, String"));
	}

	@Test
	void twoEqualMethodsWithEqualParametersShouldHaveEqualMethodSourceHashCodes() {
		assertEquals(MethodSource.from("TestClass1", "testMethod1", "int, String").hashCode(),
			MethodSource.from("TestClass1", "testMethod1", "int, String").hashCode());
	}

	@Test
	void twoEqualMethodsWithUnequalParametersShouldHaveUnequalMethodSourceHashCodes() {
		assertNotEquals(MethodSource.from("TestClass1", "testMethod1", "int, String").hashCode(),
			MethodSource.from("TestClass1", "testMethod1", "float, int, String"));
	}

	@Test
	void aReflectedMethodsClassNameShouldBeConsistent() throws Exception {
		Method m = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals("java.lang.String", MethodSource.from(m).getClassName());
	}

	@Test
	void aReflectedMethodsMethodNameShouldBeConsistent() throws Exception {
		Method m = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals("valueOf", MethodSource.from(m).getMethodName());
	}

	@Test
	void aReflectedMethodsParameterTypesShouldBeConsistent() throws Exception {
		Method m = String.class.getDeclaredMethod("valueOf", float.class);

		assertEquals("float", MethodSource.from(m).getMethodParameterTypes());
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceObjects() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals(MethodSource.from(m1), MethodSource.from(m2));
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceHashCodes() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals(MethodSource.from(m1).hashCode(), MethodSource.from(m2).hashCode());
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceObjects() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = Byte.class.getDeclaredMethod("byteValue");

		assertNotEquals(MethodSource.from(m1), MethodSource.from(m2));
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceHashCodes() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = Byte.class.getDeclaredMethod("byteValue");

		assertNotEquals(MethodSource.from(m1).hashCode(), MethodSource.from(m2).hashCode());
	}

}
