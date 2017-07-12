/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterEachMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeEachMethods;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.commons.JUnitException;

class LifecycleMethodUtilsTest {

	@Test
	void findNonVoidBeforeAllMethodsWithStandardLifecycle() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findBeforeAllMethods(TestCaseWithNonVoidLifecyleMethods.class, true));
		assertEquals(
			"@BeforeAll method 'java.lang.Double org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.cc()' must not return a value.",
			exception.getMessage());
	}

	@Test
	void findNonVoidAfterAllMethodsWithStandardLifecycle() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findAfterAllMethods(TestCaseWithNonVoidLifecyleMethods.class, true));
		assertEquals(
			"@AfterAll method 'java.lang.String org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.dd()' must not return a value.",
			exception.getMessage());
	}

	@Test
	void findNonVoidBeforeEachMethodsWithStandardLifecycle() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findBeforeEachMethods(TestCaseWithNonVoidLifecyleMethods.class));
		assertEquals(
			"@BeforeEach method 'java.lang.String org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.aa()' must not return a value.",
			exception.getMessage());
	}

	@Test
	void findNonVoidAfterEachMethodsWithStandardLifecycle() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findAfterEachMethods(TestCaseWithNonVoidLifecyleMethods.class));
		assertEquals(
			"@AfterEach method 'int org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.bb()' must not return a value.",
			exception.getMessage());
	}

	@Test
	void findBeforeEachMethodsWithStandardLifecycle() {
		List<Method> methods = findBeforeEachMethods(TestCaseWithStandardLifecycle.class);

		assertEquals(2, methods.size());
		assertTrue(contains(methods, "nine"));
		assertTrue(contains(methods, "ten"));
	}

	@Test
	void findAfterEachMethodsWithStandardLifecycle() {
		List<Method> methods = findAfterEachMethods(TestCaseWithStandardLifecycle.class);

		assertEquals(2, methods.size());
		assertTrue(contains(methods, "eleven"));
		assertTrue(contains(methods, "twelve"));
	}

	@Test
	void findBeforeAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
		List<Method> methods = findBeforeAllMethods(TestCaseWithStandardLifecycle.class, false);

		assertEquals(1, methods.size());
		assertTrue(contains(methods, "one"));
	}

	@Test
	void findBeforeAllMethodsWithStandardLifecycleAndRequiringStatic() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findBeforeAllMethods(TestCaseWithStandardLifecycle.class, true));
		assertEquals(
			"@BeforeAll method 'void org.junit.jupiter.engine.descriptor.TestCaseWithStandardLifecycle.one()' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
			exception.getMessage());
	}

	@Test
	void findBeforeAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
		List<Method> methods = findBeforeAllMethods(TestCaseWithLifecyclePerClass.class, false);

		assertEquals(2, methods.size());
		assertTrue(contains(methods, "three"));
		assertTrue(contains(methods, "four"));
	}

	@Test
	void findAfterAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
		List<Method> methods = findAfterAllMethods(TestCaseWithStandardLifecycle.class, false);

		assertEquals(2, methods.size());
		assertTrue(contains(methods, "five"));
		assertTrue(contains(methods, "six"));
	}

	@Test
	void findAfterAllMethodsWithStandardLifecycleAndRequiringStatic() {
		assertThrows(JUnitException.class, () -> findAfterAllMethods(TestCaseWithStandardLifecycle.class, true));
	}

	@Test
	void findAfterAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
		List<Method> methods = findAfterAllMethods(TestCaseWithLifecyclePerClass.class, false);

		assertEquals(2, methods.size());
		assertTrue(contains(methods, "seven"));
		assertTrue(contains(methods, "eight"));
	}

	private boolean contains(List<Method> methods, String methodName) {
		// @formatter:off
        return methods.stream()
                .map(Method::getName)
                .anyMatch(name -> name.equals(methodName));
        // @formatter:on
	}

}

class TestCaseWithStandardLifecycle {

	@BeforeAll
	void one() {
	}

	@BeforeEach
	void nine() {
	}

	@BeforeEach
	void ten() {
	}

	@AfterEach
	void eleven() {
	}

	@AfterEach
	void twelve() {
	}

	@AfterAll
	void five() {
	}

	@AfterAll
	void six() {
	}

}

@TestInstance(Lifecycle.PER_CLASS)
class TestCaseWithLifecyclePerClass {

	@BeforeAll
	void three() {
	}

	@BeforeAll
	void four() {
	}

	@AfterAll
	void seven() {
	}

	@AfterAll
	void eight() {
	}

}

class TestCaseWithNonVoidLifecyleMethods {

	@BeforeEach
	String aa() {
		return null;
	}

	@AfterEach
	int bb() {
		return 1;
	}

	@BeforeAll
	Double cc() {
		return null;
	}

	@AfterAll
	String dd() {
		return "";
	}

}
