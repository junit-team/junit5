/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.util;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.Theory;
import org.junit.jupiter.theories.TheoryDisplayNameFormatter;
import org.junit.jupiter.theories.TheoryInvocationContext;
import org.junit.jupiter.theories.domain.DataPointDetails;

/**
 * Tests for {@link TheoryDisplayNameFormatter}.
 */
class TheoryDisplayNameFormatterTests {
	@Test
	public void testEndToEnd() throws Exception {
		//Setup
		String displayName = "testDisplayName";
		int currentPermutationIndex = 0;
		int totalPermutations = 42;
		ArgumentUtils mockArgumentUtils = mock(ArgumentUtils.class);
		Method testMethod = TheoryDisplayNameFormatterTests.class.getMethod("testMethod", String.class, String.class);

		Map<Integer, DataPointDetails> theoryParameterArguments = new HashMap<>();
		theoryParameterArguments.put(0, new DataPointDetails("foo", Collections.emptyList(), "testSource"));
		theoryParameterArguments.put(1, new DataPointDetails("bar", Collections.emptyList(), "testSource"));

		TheoryInvocationContext invocationContext = new TheoryInvocationContext(currentPermutationIndex,
			theoryParameterArguments, null, testMethod, mockArgumentUtils);

		String testArgumentDescription = "Test argument description";
		when(mockArgumentUtils.getArgumentsDescriptions(eq(testMethod), eq(theoryParameterArguments),
			eq(", "))).thenReturn(testArgumentDescription);

		InputExpectedResultValuePair inputAndExpectedResult = buildInputAndExpectedResultStrings(
			new InputExpectedResultValuePair(Theory.DISPLAY_NAME_PLACEHOLDER, displayName),
			new InputExpectedResultValuePair(Theory.CURRENT_PERMUTATION_PLACEHOLDER,
				Integer.toString(currentPermutationIndex + 1)),
			new InputExpectedResultValuePair(Theory.TOTAL_PERMUTATIONS_PLACEHOLDER,
				Integer.toString(totalPermutations)),
			new InputExpectedResultValuePair(Theory.PARAMETER_VALUES_PLACEHOLDER,
				theoryParameterArguments.entrySet().stream().map(v -> (String) v.getValue().getValue()).collect(
					joining(", "))),
			new InputExpectedResultValuePair(Theory.PARAMETER_VALUES_WITH_INDEXES_PLACEHOLDER,
				theoryParameterArguments.entrySet().stream().map(
					v -> v.getValue().getValue() + " (index " + v.getKey() + ")").collect(joining(", "))),
			new InputExpectedResultValuePair(Theory.PARAMETER_DETAILS_PLACEHOLDER, testArgumentDescription));

		//Test
		TheoryDisplayNameFormatter formatterUnderTest = new TheoryDisplayNameFormatter(inputAndExpectedResult.input,
			displayName, totalPermutations, mockArgumentUtils);
		String actualResult = formatterUnderTest.format(invocationContext);

		//Verify
		assertEquals(inputAndExpectedResult.expectedResult, actualResult);
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	private InputExpectedResultValuePair buildInputAndExpectedResultStrings(InputExpectedResultValuePair... entries) {
		String input = Stream.of(entries).map(v -> v.input).collect(joining("] [", "[", "]"));
		String expectedResult = Stream.of(entries).map(v -> v.expectedResult).collect(joining("] [", "[", "]"));

		return new InputExpectedResultValuePair(input, expectedResult);
	}

	public void testMethod(String a, String b) {
		//Empty method to provide a valid target for the invocation context
	}

	private static class InputExpectedResultValuePair {
		private final String input;
		private final String expectedResult;

		public InputExpectedResultValuePair(String input, String expectedResult) {
			this.input = input;
			this.expectedResult = expectedResult;
		}
	}
}
