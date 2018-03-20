
package org.junit.jupiter.theories.util;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.TheoryInvocationContext;
import org.junit.jupiter.theories.annotations.Theory;
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

		Map<Integer, DataPointDetails> theoryParameterArguments = new HashMap<>();
		theoryParameterArguments.put(0, new DataPointDetails("foo", Collections.emptyList(), "testSource"));
		theoryParameterArguments.put(1, new DataPointDetails("bar", Collections.emptyList(), "testSource"));

		TheoryInvocationContext invocationContext = new TheoryInvocationContext(currentPermutationIndex,
			theoryParameterArguments, null,
			TheoryDisplayNameFormatterTests.class.getMethod("testMethod", String.class, String.class));

		InputExpectedResultValuePair inputAndExpectedResult = buildInputAndExpectedResulStrings(
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
			new InputExpectedResultValuePair(Theory.PARAMETER_DETAILS_PLACEHOLDER,
				invocationContext.getArgumentsDescription(", ")));

		//Test
		TheoryDisplayNameFormatter formatterUnderTest = new TheoryDisplayNameFormatter(inputAndExpectedResult.input,
			displayName, totalPermutations);
		String actualResult = formatterUnderTest.format(invocationContext);

		//Verify
		assertEquals(inputAndExpectedResult.expectedResult, actualResult);
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	private InputExpectedResultValuePair buildInputAndExpectedResulStrings(InputExpectedResultValuePair... entries) {
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
