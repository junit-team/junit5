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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;
import org.junit.jupiter.theories.suppliers.ArgumentsSuppliedBy;
import org.junit.jupiter.theories.suppliers.TheoryArgumentSupplier;

class ArgumentSupplierUtilsTests {

	private static boolean testArgumentSupplierThrowsException;
	private static String TEST_EXCEPTION_MESSAGE = "Test exception";
	private static BiFunction<TheoryParameterDetails, Annotation, List<DataPointDetails>> mockSupplierFunction;
	private ArgumentSupplierUtils utilsUnderTest;

	private static String[] getAnnotationTestValue() {
		String[] value = { "foo", "bar" };
		return value;
	}

	@BeforeEach
	public void setUp() {
		ArgumentSupplierUtils.clearCache();
		mockSupplierFunction = ((a, b) -> Collections.emptyList());

		utilsUnderTest = new ArgumentSupplierUtils();
		testArgumentSupplierThrowsException = false;
	}

	@Test
	public void testGetParameterSupplierAnnotation_AnnotationPresent() throws Exception {
		//Setup
		Parameter parameter = TestMethodSource.class.getMethod("methodWithAnnotation", String.class).getParameters()[0];

		//Test
		Optional<? extends Annotation> result = utilsUnderTest.getParameterSupplierAnnotation(parameter);

		//Verify
		// @formatter:off
		assertThat(result)
				.isPresent()
				.hasValueSatisfying(rawAnnotation -> assertThat(rawAnnotation)
						.isInstanceOf(TestArgumentSupplierAnnotation.class));
		// @formatter:on

		TestArgumentSupplierAnnotation annotationFromResult = (TestArgumentSupplierAnnotation) result.get();
		assertThat(annotationFromResult.value()).containsExactly(getAnnotationTestValue());
	}

	@Test
	public void testGetParameterSupplierAnnotation_AnnotationMissing() throws Exception {
		//Setup
		Parameter parameter = TestMethodSource.class.getMethod("methodWithoutAnnotation",
			String.class).getParameters()[0];

		//Test
		Optional<? extends Annotation> result = utilsUnderTest.getParameterSupplierAnnotation(parameter);

		//Verify
		assertThat(result).isEmpty();
	}

	@Test
	public void testBuildDataPointDetailsFromParameterSupplierAnnotation_Error_InvalidTheoryParameterDetails() {
		//Setup
		TheoryParameterDetails parameterDetails = new TheoryParameterDetails(0, Object.class, "testParameter",
			Arrays.asList("foo", "bar"), Optional.empty());

		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> utilsUnderTest.buildDataPointDetailsFromParameterSupplierAnnotation("testMethod", parameterDetails))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("did not contain a argument supplier annotation");
		// @formatter:on
	}

	@Test
	public void testBuildDataPointDetailsFromParameterSupplierAnnotation_Error_SupplierInstantiationFailure()
			throws Exception {
		//Setup
		Annotation supplierAnnotation = TestMethodSource.class.getMethod("methodWithAnnotation",
			String.class).getParameters()[0].getAnnotation(TestArgumentSupplierAnnotation.class);
		testArgumentSupplierThrowsException = true;
		TheoryParameterDetails parameterDetails = new TheoryParameterDetails(0, Object.class, "testParameter",
			Arrays.asList("foo", "bar"), Optional.of(supplierAnnotation));

		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> utilsUnderTest.buildDataPointDetailsFromParameterSupplierAnnotation("testMethod", parameterDetails))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Unable to instantiate parameter argument supplier")
				.hasMessageContaining(TEST_EXCEPTION_MESSAGE);
		// @formatter:on
	}

	@Test
	public void testBuildDataPointDetailsFromParameterSupplierAnnotation_Error_SupplierReturnsWrongType()
			throws Exception {
		//Setup
		Annotation supplierAnnotation = TestMethodSource.class.getMethod("methodWithAnnotation",
			String.class).getParameters()[0].getAnnotation(TestArgumentSupplierAnnotation.class);
		TheoryParameterDetails parameterDetails = new TheoryParameterDetails(0, String.class, "testParameter",
			Arrays.asList("foo", "bar"), Optional.of(supplierAnnotation));

		List<DataPointDetails> dataPointDetails = Arrays.asList(
			new DataPointDetails("foo", Collections.emptyList(), "testSource"),
			new DataPointDetails(42, Collections.emptyList(), "testSource"), //Incorrect type for this parameter
			new DataPointDetails("bar", Collections.emptyList(), "testSource"));
		mockSupplierFunction = (a, b) -> dataPointDetails;

		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> utilsUnderTest.buildDataPointDetailsFromParameterSupplierAnnotation("testMethod", parameterDetails))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("returned incorrect type(s)");
		// @formatter:on
	}

	@Test
	public void testBuildDataPointDetailsFromParameterSupplierAnnotation_Success() throws Exception {
		//Setup
		Annotation supplierAnnotation = TestMethodSource.class.getMethod("methodWithAnnotation",
			String.class).getParameters()[0].getAnnotation(TestArgumentSupplierAnnotation.class);
		TheoryParameterDetails parameterDetails = new TheoryParameterDetails(0, String.class, "testParameter",
			Arrays.asList("foo", "bar"), Optional.of(supplierAnnotation));

		List<DataPointDetails> dataPointDetails = Arrays.asList(
			new DataPointDetails("foo", Collections.emptyList(), "testSource"),
			new DataPointDetails("bar", Collections.emptyList(), "testSource"),
			new DataPointDetails("baz", Collections.emptyList(), "testSource"));
		mockSupplierFunction = (actualParameterDetails, actualSupplierAnnotation) -> {
			assertEquals(parameterDetails, actualParameterDetails);
			assertEquals(supplierAnnotation, actualSupplierAnnotation);
			return dataPointDetails;
		};

		//Test
		List<DataPointDetails> result = utilsUnderTest.buildDataPointDetailsFromParameterSupplierAnnotation(
			"testMethod", parameterDetails);

		//Verify
		assertEquals(dataPointDetails, result);
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	@Retention(RetentionPolicy.RUNTIME)
	@ArgumentsSuppliedBy(TestTheoryArgumentSupplier.class)
	private @interface TestArgumentSupplierAnnotation {
		String[] value();
	}

	private static class TestTheoryArgumentSupplier implements TheoryArgumentSupplier {
		public TestTheoryArgumentSupplier() {
			if (testArgumentSupplierThrowsException) {
				throw new RuntimeException(TEST_EXCEPTION_MESSAGE);
			}
		}

		@Override
		public List<DataPointDetails> buildArgumentsFromSupplierAnnotation(TheoryParameterDetails parameterDetails,
				Annotation annotationToParse) {
			return mockSupplierFunction.apply(parameterDetails, annotationToParse);
		}
	}

	private static class TestMethodSource {
		public void methodWithoutAnnotation(String parameter) {
		}

		public void methodWithAnnotation(@TestArgumentSupplierAnnotation({ "foo", "bar" }) String parameter) {
		}
	}
}
