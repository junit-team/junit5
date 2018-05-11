/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.theories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.theories.DataPoint;
import org.junit.jupiter.theories.DataPoints;
import org.junit.jupiter.theories.Qualifiers;
import org.junit.jupiter.theories.Theory;
import org.junit.jupiter.theories.suppliers.IntValues;

public class TheoriesTestDemo {

	@Nested
	public static class basicTheoryExample {
		// tag::basic_usage_example[]
		@DataPoint
		private static final String ALPHA_ONLY = "Hello world";

		@DataPoint
		private static final String NUMERIC = "1234";

		@DataPoint
		private static final String SPECIAL_CHARACTERS = "!@#$%^&*";

		@Theory
		public void testStringBuilder_UseConstructor(String value1, String value2) {
			String expectedValue = value1 + value2;
			String actualValue = new StringBuilder(value1).append(value2).toString();
			assertEquals(expectedValue, actualValue);
		}

		@Theory
		public void testStringBuilder_AppendOnly(String value1, String value2) {
			String expectedValue = value1 + value2;
			String actualValue = new StringBuilder().append(value1).append(value2).toString();
			assertEquals(expectedValue, actualValue);
		}
		// end::basic_usage_example[]
	}

	@Nested
	public static class qualifiersExample {
		// tag::qualifiers_example[]
		@DataPoints(qualifiers = "nonZero")
		private static final List<Integer> GREATER_THAN_ZERO = Arrays.asList(3, 5, 7);

		@DataPoint
		private static final int ZERO = 0;

		@DataPoint(qualifiers = "nonZero")
		private static final int NEGATIVE_NUMBER = -4;

		@Theory
		public void testBigIntegerDivision(int dividend, @Qualifiers("nonZero") int divisor) {
			double expectedValue = dividend / divisor;
			BigInteger result = BigInteger.valueOf(dividend).divide(BigInteger.valueOf(divisor));
			assertEquals(expectedValue, result.intValue());
		}
		// end::qualifiers_example[]
	}

	@Nested
	public static class argumentSupplierExample {
		// tag::argument_supplier_example[]
		private static final List<Integer> INTEGER_VALUES = Arrays.asList(-10, -5, 0, 3, 5, 7);

		@Theory
		public void testBigIntegerDivision(@IntValues({ 1, 2, 3, 4 }) int value) {
		}
		// end::argument_supplier_example[]
	}

	@Nested
	public static class customDisplayNameExample {
		// @formatter:off
		// tag::custom_display_names[]
		@DataPoints
		private static final List<Integer> INTEGER_VALUES = Arrays.asList(3, 5, 7);

		@DisplayName("Overall theory name")
		@Theory(name = "{name} ({currentPermutation}/{totalPermutations}) " +
				"=> {argumentValuesWithIndices}")
		public void testTheoryWithCustomDisplayName(int first, int second) {
		}
		// end::custom_display_names[]
		// @formatter:on
	}

	@Nested
	public static class customSupplierExample {
		// tag::custom_supplier_example_supplier[]
		@Theory
		public void testMyGui_ClickOnLocations(@XYPointValues({ "0,0", "20,10", "100,100" }) XYPoint pointToClick) {
			//...
		}
		// end::custom_supplier_example_supplier[]
	}
}
