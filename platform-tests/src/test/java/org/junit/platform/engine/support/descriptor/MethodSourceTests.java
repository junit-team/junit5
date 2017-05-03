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

/**
 * Unit tests for {@link MethodSource}.
 *
 * @since 1.0
 */
class MethodSourceTests {

	@Test
	void instantiatingWithNullNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("foo", null));
		assertThrows(PreconditionViolationException.class, () -> new MethodSource(null, "foo"));
	}

	@Test
	void instantiatingWithEmptyNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("foo", ""));
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("", "foo"));
	}

	@Test
	void instantiatingWithBlankNamesShouldThrowPreconditionViolationException() {
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("foo", "  "));
		assertThrows(PreconditionViolationException.class, () -> new MethodSource("  ", "foo"));
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
	void twoEqualMethodSourceObjectsShouldHaveEqualHashCodes() {
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
		Method m = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals("java.lang.String", new MethodSource(m).getClassName());
	}

	@Test
	void aReflectedMethodsMethodNameShouldBeConsistent() throws Exception {
		Method m = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals("valueOf", new MethodSource(m).getMethodName());
	}

	@Test
	void aReflectedMethodsParameterTypesShouldBeConsistent() throws Exception {
		Method m = String.class.getDeclaredMethod("valueOf", float.class);

		assertEquals("float", new MethodSource(m).getMethodParameterTypes());
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceObjects() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals(new MethodSource(m1), new MethodSource(m2));
	}

	@Test
	void twoEqualReflectedMethodsShouldHaveEqualMethodSourceHashCodes() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = String.class.getDeclaredMethod("valueOf", int.class);

		assertEquals(new MethodSource(m1).hashCode(), new MethodSource(m2).hashCode());
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceObjects() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = Byte.class.getDeclaredMethod("byteValue");

		assertNotEquals(new MethodSource(m1), new MethodSource(m2));
	}

	@Test
	void twoUnequalReflectedMethodsShouldNotHaveEqualMethodSourceHashCodes() throws Exception {
		Method m1 = String.class.getDeclaredMethod("valueOf", int.class);
		Method m2 = Byte.class.getDeclaredMethod("byteValue");

		assertNotEquals(new MethodSource(m1).hashCode(), new MethodSource(m2).hashCode());
	}

}
