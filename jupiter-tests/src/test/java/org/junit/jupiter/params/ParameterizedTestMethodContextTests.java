/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.aggregator.AggregatorIntegrationTests.CsvToPerson;
import org.junit.jupiter.params.aggregator.AggregatorIntegrationTests.Person;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ParameterizedTestMethodContext}.
 *
 * @since 5.2
 */
class ParameterizedTestMethodContextTests {

	@ParameterizedTest
	@ValueSource(strings = { "onePrimitive", "twoPrimitives", "twoAggregators", "twoAggregatorsWithTestInfoAtTheEnd",
			"mixedMode" })
	void validSignatures(String name) {
		assertTrue(new ParameterizedTestMethodContext(method(name)).hasPotentiallyValidSignature());
	}

	@ParameterizedTest
	@ValueSource(strings = { "twoAggregatorsWithPrimitiveInTheMiddle", "twoAggregatorsWithTestInfoInTheMiddle" })
	void invalidSignatures(String name) {
		assertFalse(new ParameterizedTestMethodContext(method(name)).hasPotentiallyValidSignature());
	}

	private Method method(String name) {
		return Arrays.stream(getClass().getDeclaredMethods()) //
				.filter(m -> m.getName().equals(name)) //
				.findFirst() //
				.orElseThrow();
	}

	// --- VALID ---------------------------------------------------------------

	void onePrimitive(int num) {
	}

	void twoPrimitives(int num1, int num2) {
	}

	void twoAggregators(@CsvToPerson Person person, ArgumentsAccessor arguments) {
	}

	void twoAggregatorsWithTestInfoAtTheEnd(@CsvToPerson Person person1, @CsvToPerson Person person2,
			TestInfo testInfo) {
	}

	void mixedMode(int num1, int num2, ArgumentsAccessor arguments1, ArgumentsAccessor arguments2,
			@CsvToPerson Person person1, @CsvToPerson Person person2, TestInfo testInfo1, TestInfo testInfo2) {
	}

	// --- INVALID -------------------------------------------------------------

	void twoAggregatorsWithPrimitiveInTheMiddle(@CsvToPerson Person person1, int num, @CsvToPerson Person person2) {
	}

	void twoAggregatorsWithTestInfoInTheMiddle(@CsvToPerson Person person1, TestInfo testInfo,
			@CsvToPerson Person person2) {
	}

}
