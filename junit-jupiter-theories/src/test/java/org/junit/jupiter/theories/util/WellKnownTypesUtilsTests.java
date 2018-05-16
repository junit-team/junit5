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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Tests for {@link WellKnownTypesUtils}.
 */
class WellKnownTypesUtilsTests {

	private static final List<WellKnownTypeTestCase> TEST_CASES = Arrays.asList(
		new WellKnownTypeTestCase(Boolean.class,
			new DataPointDetails(true, emptyList(), "Automatic boolean data point generation"),
			new DataPointDetails(false, emptyList(), "Automatic boolean data point generation")),
		new WellKnownTypeTestCase(TestEnum.class,
			new DataPointDetails(TestEnum.FOO, emptyList(), "Automatic enum data point generation"),
			new DataPointDetails(TestEnum.BAR, emptyList(), "Automatic enum data point generation"),
			new DataPointDetails(TestEnum.BAZ, emptyList(), "Automatic enum data point generation")));

	private WellKnownTypesUtils utilsUnderTest;

	@BeforeEach
	public void beforeEach() {
		utilsUnderTest = new WellKnownTypesUtils();
	}

	@Test
	public void testIsKnownType_Known() {
		for (WellKnownTypeTestCase currTestCase : TEST_CASES) {
			//Test
			boolean result = utilsUnderTest.isKnownType(currTestCase.parameterType);

			//Verify
			assertTrue(result);
		}
	}

	@Test
	public void testIsKnownType_NotKnown() {
		//Test
		boolean result = utilsUnderTest.isKnownType(String.class);

		//Verify
		assertFalse(result);
	}

	@Test
	public void testIsKnownType_PrimitiveTypeNotAccepted() {
		//Test/Verify
		assertThatThrownBy(() -> utilsUnderTest.isKnownType(int.class))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("does not accept primitives");
	}

	@Test
	public void testGetDataPointDetails_Known() {
		for (WellKnownTypeTestCase currTestCase : TEST_CASES) {
			//Setup
			TheoryParameterDetails details = new TheoryParameterDetails(0, currTestCase.parameterType, "testParameter",
				emptyList(), Optional.empty());

			//Test
			Optional<List<DataPointDetails>> result = utilsUnderTest.getDataPointDetails(details);

			//Verify
			// @formatter:off
			assertThat(result)
					.isPresent()
					.hasValueSatisfying(v -> assertThat(v)
							.containsExactlyInAnyOrderElementsOf(currTestCase.expectedDataPointDetails));
			// @formatter:on
		}
	}

	@Test
	public void testGetDataPointDetails_NotKnown() {
		//Setup
		TheoryParameterDetails details = new TheoryParameterDetails(0, String.class, "testParameter", emptyList(),
			Optional.empty());

		//Test
		Optional<List<DataPointDetails>> result = utilsUnderTest.getDataPointDetails(details);

		//Verify
		assertThat(result).isEmpty();
	}

	private enum TestEnum {
		FOO, BAR, BAZ
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	private static class WellKnownTypeTestCase {
		private final Class<?> parameterType;
		private final List<DataPointDetails> expectedDataPointDetails;

		public WellKnownTypeTestCase(Class<?> parameterType, DataPointDetails... expectedDataPointDetails) {
			this.parameterType = parameterType;
			this.expectedDataPointDetails = Collections.unmodifiableList(Arrays.asList(expectedDataPointDetails));
		}
	}
}
