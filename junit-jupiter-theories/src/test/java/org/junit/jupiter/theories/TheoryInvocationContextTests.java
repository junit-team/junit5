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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.extensions.TheoryParameterResolver;
import org.junit.jupiter.theories.util.ArgumentUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link TheoryInvocationContext}.
 */
class TheoryInvocationContextTests {
	private static final int TEST_PERMUTATION_INDEX = 42;

	@Mock
	private TheoryDisplayNameFormatter mockDisplayNameFormatter;

	@Mock
	private ArgumentUtils mockArgumentUtils;

	private Method testTargetMethod;

	private Map<Integer, DataPointDetails> theoryParameterArguments;

	private TheoryInvocationContext contextUnderTest;

	@BeforeEach
	public void beforeEach() throws Exception {
		MockitoAnnotations.initMocks(this);

		//Actual method doesn't matter here--we just need a valid method to test with
		testTargetMethod = String.class.getMethod("getBytes", int.class, int.class, byte[].class, int.class);

		byte[] byteArgument = { (byte) 1, (byte) 2 };

		theoryParameterArguments = new HashMap<>();
		theoryParameterArguments.put(0, new DataPointDetails(123, Collections.emptyList(), "Test source 1"));
		theoryParameterArguments.put(1, new DataPointDetails(byteArgument, Collections.emptyList(), "Test source 2"));
		theoryParameterArguments.put(2, new DataPointDetails(321, Collections.emptyList(), "Test source 3"));

		contextUnderTest = new TheoryInvocationContext(TEST_PERMUTATION_INDEX, theoryParameterArguments,
			mockDisplayNameFormatter, testTargetMethod, mockArgumentUtils);
	}

	@Test
	public void testGetDisplayName() {
		//Setup
		String expectedResult = "Expected display name";
		when(mockDisplayNameFormatter.format(contextUnderTest)).thenReturn(expectedResult);

		int ignored = 0;

		//Test
		String actualResult = contextUnderTest.getDisplayName(ignored);

		//Verify
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testGetPermutationIndex() {
		//Test
		int result = contextUnderTest.getPermutationIndex();

		//Verify
		assertEquals(TEST_PERMUTATION_INDEX, result);
	}

	@Test
	public void testGetTheoryParameterArguments() {
		//Test
		Map<Integer, DataPointDetails> result = contextUnderTest.getTheoryParameterArguments();

		//Verify
		assertEquals(theoryParameterArguments, result);
	}

	@Test
	public void testGetTestMethod() {
		//Test
		Method result = contextUnderTest.getTestMethod();

		//Verify
		assertEquals(testTargetMethod, result);
	}

	@Test
	public void testGetAdditionalExtensions() {
		//Test
		List<Extension> result = contextUnderTest.getAdditionalExtensions();

		//Verify
		assertThat(result).hasSize(2);

		// @formatter:off
		Optional<TheoryParameterResolver> parameterResolver = result.stream()
				.filter(v -> v instanceof TheoryParameterResolver)
				.map(v -> (TheoryParameterResolver)v)
				.findFirst();
		assertTrue(parameterResolver.isPresent());

		Optional<TheoryParameterResolver> TheoryTestFailureMessageFixer = result.stream()
				.filter(v -> v instanceof TheoryParameterResolver)
				.map(v -> (TheoryParameterResolver)v)
				.findFirst();
		assertTrue(parameterResolver.isPresent());
		// @formatter:on
	}
}
