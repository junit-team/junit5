/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.theories.Theory.ARGUMENT_VALUES_PLACEHOLDER;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.theories.suppliers.DoubleValues;
import org.junit.jupiter.theories.suppliers.IntValues;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TheoriesTestCase {

	@DataPoint
	public static final int normalDataPoint = 1;

	@DataPoint(qualifiers = "multipleOf2")
	public static final int qualifiedDataPoint = 2;

	@DataPoints
	public static final List<Integer> boxedCollectionDataPoint = Arrays.asList(3, 5, 7);

	@DataPoint
	public static final double double1 = 1.0;

	@DataPoint
	public static final double double2 = 2.0;

	@DataPoint
	public static final double double3 = 3.0;

	@DataPoint(qualifiers = "multipleOf2")
	public final int nonStaticDataPoint = 4;

	private final ExampleClass objectUnderTest;

	public TheoriesTestCase() {
		this.objectUnderTest = new ExampleClass();
	}

	@DataPoint
	public static int dataPointMethod() {
		return 9;
	}

	/**
	 * Basic test to ensure that we didn't break non-theory tests somehow.
	 */
	@Test
	public void testAdd_TwoIntegers() {
		//Test
		int result = objectUnderTest.add(2, 2);

		//Verify
		assertEquals(4, result);
	}

	@Theory
	public void testAdd_Ints_Theory_WithQualifier(@Qualifiers("multipleOf2") int x, int y) {
		//Test
		int result = objectUnderTest.add(x, y);

		//Verify
		assertEquals(x + y, result);
	}

	@Theory
	public void testAdd_Ints_Theory(int x, int y) {
		//Test
		int result = objectUnderTest.add(x, y);

		//Verify
		assertEquals(x + y, result);
	}

	@Theory(name = ARGUMENT_VALUES_PLACEHOLDER)
	public void testAdd_Ints_TheoryWithEnum(TestEnum x, TestEnum y) {
		//Test
		int result = objectUnderTest.add(x.value, y.value);

		//Verify
		assertEquals(x.value + y.value, result);
	}

	@Theory
	public void testAdd_Ints_Theory_WithParamSupplier(@IntValues({ 1, 2, 3 }) int x, @IntValues({ 10, 20, 30 }) int y) {

		//Test
		int result = objectUnderTest.add(x, y);

		//Verify
		assertEquals(x + y, result);
	}

	@Theory
	public void testAdd_IntAndDouble_Theory(int x, double y) {
		//Test
		double result = objectUnderTest.add(x, y);

		//Verify
		assertEquals(x + y, result);
	}

	@Theory
	public void testAdd_IntAndDouble_TheoryWithParamSupplier(@IntValues({ 1, 2, 3 }) int x,
			@DoubleValues({ 10.0, 20.0, 30.0 }) double y) {
		//Test
		double result = objectUnderTest.add(x, y);

		//Verify
		assertEquals(x + y, result);
	}

	@DisplayName("Test")
	@Theory(name = Theory.DISPLAY_NAME_PLACEHOLDER + " (" + Theory.CURRENT_PERMUTATION_PLACEHOLDER + "/"
			+ Theory.TOTAL_PERMUTATIONS_PLACEHOLDER + ") => " + Theory.ARGUMENT_VALUES_WITH_INDEXES_PLACEHOLDER)
//	@Disabled("Failure case. Enable to simulate a test failure")
	public void testThrowException(int x, double y) {
		//Test
		if (x == 2) {
			throw new RuntimeException("Test exception");
		}
	}

	public enum TestEnum {
		ONE(1), TWO(2), THREE(3);

		private final int value;

		TestEnum(int value) {
			this.value = value;
		}
	}

	public static class ExampleClass {
		public int add(int x, int y) {
			return x + y;
		}

		public double add(int x, double y) {
			return x + y;
		}
	}
}
