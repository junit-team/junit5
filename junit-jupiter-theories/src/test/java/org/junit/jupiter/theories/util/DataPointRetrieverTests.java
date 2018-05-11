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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.DataPoint;
import org.junit.jupiter.theories.DataPointRetriever;
import org.junit.jupiter.theories.DataPoints;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;

class DataPointRetrieverTests {

	private DataPointRetriever retrieverUnderTest;

	@BeforeEach
	public void setUp() {
		retrieverUnderTest = new DataPointRetriever();
	}

	@Test
	public void testGetAllDataPoints_InstanceDataPoints() {
		//Setup
		ClassWithValidInstanceDataPoints dataPointsSourceObject = new ClassWithValidInstanceDataPoints();
		List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

		//Test
		List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(
			ClassWithValidInstanceDataPoints.class, Optional.of(dataPointsSourceObject));

		//Verify
		// @formatter:off
		List<String> actualValues = actualResults.stream()
				.map(DataPointDetails::getValue)
				.peek(v -> assertThat(v).isInstanceOf(String.class))
				.map(v -> (String) v)
				.collect(toList());
		// @formatter:on

		assertThat(actualValues).containsExactlyInAnyOrderElementsOf(expectedResults);
	}

	@Test
	public void testGetAllDataPoints_StaticDataPoints_WithInstance() {
		//Setup
		ClassWithValidStaticDataPoints dataPointsSourceObject = new ClassWithValidStaticDataPoints();
		List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

		//Test
		List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(ClassWithValidStaticDataPoints.class,
			Optional.of(dataPointsSourceObject));

		//Verify
		// @formatter:off
		List<String> actualValues = actualResults.stream()
				.map(DataPointDetails::getValue)
				.peek(v -> assertThat(v).isInstanceOf(String.class))
				.map(v -> (String) v)
				.collect(toList());
		// @formatter:on

		assertThat(actualValues).containsExactlyInAnyOrderElementsOf(expectedResults);
	}

	@Test
	public void testGetAllDataPoints_StaticDataPoints_WithoutInstance() {
		//Setup
		ClassWithValidStaticDataPoints dataPointsSourceObject = new ClassWithValidStaticDataPoints();
		List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

		//Test
		List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(ClassWithValidStaticDataPoints.class,
			Optional.empty());

		//Verify
		// @formatter:off
		List<String> actualValues = actualResults.stream()
				.map(DataPointDetails::getValue)
				.peek(v -> assertThat(v).isInstanceOf(String.class))
				.map(v -> (String) v)
				.collect(toList());
		// @formatter:on

		assertThat(actualValues).containsExactlyInAnyOrderElementsOf(expectedResults);
	}

	@Test
	public void testGetAllDataPoints_ThrowsExceptionOnMissingInstanceForInstanceDataPoint() {
		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithValidInstanceDataPoints.class, Optional.empty()))
				.isInstanceOf(DataPointRetrievalException.class)
				.hasMessageContaining("instance was not available");
		// @formatter:on
	}

	@Test
	public void testGetAllDataPoints_DataPointMethodThrowsException() {
		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithDataPointMethodThatThrowsException.class, Optional.empty()))
				.isInstanceOf(DataPointRetrievalException.class)
				.hasMessageContaining(ClassWithDataPointMethodThatThrowsException.EXCEPTION_MESSAGE);
		// @formatter:on
	}

	@Test
	public void testGetAllDataPoints_InvalidDataPointsGroupType() {
		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithDataPointsOfInvalidGroupType.class, Optional.empty()))
				.isInstanceOf(DataPointRetrievalException.class)
				.hasMessageContaining("not a recognized group of data points");
		// @formatter:on
	}

	@Test
	public void testGetAllDataPoints_InvalidStreamField() {
		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithInvalidStreamField.class, Optional.empty()))
				.isInstanceOf(DataPointRetrievalException.class)
				.hasMessageContaining(Stream.class.getCanonicalName())
				.hasMessageContaining("only supported for data point methods");
		// @formatter:on
	}

	@Test
	public void testGetAllDataPoints_InvalidIteratorField() {
		//Test/Verify
		// @formatter:off
		assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithInvalidIteratorField.class, Optional.empty()))
				.isInstanceOf(DataPointRetrievalException.class)
				.hasMessageContaining(Iterator.class.getCanonicalName())
				.hasMessageContaining("only supported for data point methods");
		// @formatter:on
	}

	//-------------------------------------------------------------------------
	// Test helper methods/classes
	//-------------------------------------------------------------------------
	private static class ClassWithValidInstanceDataPoints {
		@DataPoint
		public final String publicInstanceStringField = "public instance string field";

		@DataPoint
		private final String privateInstanceStringField = "private instance string field";

		@DataPoints
		public final String[] publicInstanceArrayField = { "public instance array field (1/2)",
				"public instance array field (1/2)" };

		@DataPoints
		private final String[] privateInstanceArrayField = { "private instance array field (1/2)",
				"private instance array field (1/2)" };

		@DataPoints
		private final List<String> publicInstanceListField = Arrays.asList("public instance list field (1/2)",
			"public instance list field (1/2)");

		@DataPoints
		private final List<String> privateInstanceListField = Arrays.asList("private instance list field (1/2)",
			"private instance list field (1/2)");

		@DataPoint
		public String publicInstanceStringMethod() {
			return "public instance string method";
		}

		@DataPoint
		private String privateInstanceStringMethod() {
			return "private instance string method";
		}

		@DataPoints
		public String[] publicInstanceArrayMethod() {
			String[] result = { "public instance array method (1/2)", "public instance array method (2/2)" };
			return result;
		}

		@DataPoints
		private String[] privateInstanceArrayMethod() {
			String[] result = { "private instance array method (1/2)", "private instance array method (2/2)" };
			return result;
		}

		@DataPoints
		public List<String> publicInstanceListMethod() {
			return Arrays.asList("public instance list method (1/2)", "public instance list method (2/2)");
		}

		@DataPoints
		private List<String> privateInstanceListMethod() {
			return Arrays.asList("private instance list method (1/2)", "private instance list method (2/2)");
		}

		@DataPoints
		public Stream<String> publicInstanceStreamMethod() {
			return Stream.of("public instance stream method (1/2)", "public instance stream method (2/2)");
		}

		@DataPoints
		private Stream<String> privateInstanceStreamMethod() {
			return Stream.of("private instance stream method (1/2)", "private instance stream method (2/2)");
		}

		@DataPoints
		public Iterator<String> publicInstanceIteratorMethod() {
			return Arrays.asList("public instance iterable method (1/2)",
				"public instance iterable method (2/2)").iterator();
		}

		@DataPoints
		private Iterator<String> privateInstanceIteratorMethod() {
			return Arrays.asList("private instance iterable method (1/2)",
				"private instance iterable method (2/2)").iterator();
		}

		@DataPoints
		public Iterable<String> publicInstanceIterableMethod() {
			//Note: This conversion is important because it prevents the return
			//value from being an instance of Collection, which has special handling
			return () -> Arrays.asList("public instance iterable method (1/2)",
				"public instance iterable method (2/2)").iterator();
		}

		@DataPoints
		private Iterable<String> privateInstanceIterableMethod() {
			//Note: This conversion is important because it prevents the return
			//value from being an instance of Collection, which has special handling
			return () -> Arrays.asList("private instance iterable method (1/2)",
				"private instance iterable method (2/2)").iterator();
		}

		public List<String> getExpectedValues() {
			List<String> expectedValues = new ArrayList<>();

			expectedValues.add(publicInstanceStringField);
			expectedValues.add(privateInstanceStringField);
			expectedValues.addAll(Arrays.asList(publicInstanceArrayField));
			expectedValues.addAll(Arrays.asList(privateInstanceArrayField));
			expectedValues.addAll(publicInstanceListField);
			expectedValues.addAll(privateInstanceListField);

			expectedValues.add(publicInstanceStringMethod());
			expectedValues.add(privateInstanceStringMethod());
			expectedValues.addAll(Arrays.asList(publicInstanceArrayMethod()));
			expectedValues.addAll(Arrays.asList(privateInstanceArrayMethod()));
			expectedValues.addAll(publicInstanceListMethod());
			expectedValues.addAll(privateInstanceListMethod());
			expectedValues.addAll(publicInstanceStreamMethod().collect(toList()));
			expectedValues.addAll(privateInstanceStreamMethod().collect(toList()));
			copyFromIteratorToList(publicInstanceIteratorMethod(), expectedValues);
			copyFromIteratorToList(privateInstanceIteratorMethod(), expectedValues);
			copyFromIteratorToList(publicInstanceIterableMethod().iterator(), expectedValues);
			copyFromIteratorToList(privateInstanceIterableMethod().iterator(), expectedValues);

			return expectedValues;
		}

		private <T> void copyFromIteratorToList(Iterator<T> iterator, List<T> list) {
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}

	}

	private static class ClassWithValidStaticDataPoints {

		@DataPoint
		public static final String PUBLIC_STATIC_STRING_FIELD = "public static string field";

		@DataPoint
		private static final String PRIVATE_STATIC_STRING_FIELD = "private static string field";

		@DataPoints
		public static final String[] PUBLIC_STATIC_ARRAY_FIELD = { "public static array field (1/2)",
				"public static array field (2/2)" };

		@DataPoints
		private static final String[] PRIVATE_STATIC_ARRAY_FIELD = { "private static array field (1/2)",
				"private static array field (2/2)" };

		@DataPoints
		public static final List<String> PUBLIC_STATIC_LIST_FIELD = Arrays.asList("public static list field (1/2)",
			"public static list field (2/2)");

		@DataPoints
		private static final List<String> PRIVATE_STATIC_LIST_FIELD = Arrays.asList("private static list field (1/2)",
			"private static list field (2/2)");

		@DataPoint
		public static String publicStaticStringMethod() {
			return "public static string method";
		}

		@DataPoint
		private static String privateStaticStringMethod() {
			return "private static string method";
		}

		@DataPoints
		public static String[] publicStaticArrayMethod() {
			String[] result = { "public static array method (1/2)", "public static array method (2/2)" };
			return result;
		}

		@DataPoints
		private static String[] privateStaticArrayMethod() {
			String[] result = { "private static array method (1/2)", "private static array method (2/2)" };
			return result;
		}

		@DataPoints
		public static List<String> publicStaticListMethod() {
			return Arrays.asList("public static list method (1/2)", "public static list method (2/2)");
		}

		@DataPoints
		private static List<String> privateStaticListMethod() {
			return Arrays.asList("private static list method (1/2)", "private static list method (2/2)");
		}

		@DataPoints
		public static Stream<String> publicStaticStreamMethod() {
			return Stream.of("public static stream method (1/2)", "public static stream method (2/2)");
		}

		@DataPoints
		private static Stream<String> privateStaticStreamMethod() {
			return Stream.of("private static stream method (1/2)", "private static stream method (2/2)");
		}

		@DataPoints
		public static Iterator<String> publicStaticIteratorMethod() {
			return Arrays.asList("public static iterable method (1/2)",
				"public static iterable method (2/2)").iterator();
		}

		@DataPoints
		private static Iterator<String> privateStaticIteratorMethod() {
			return Arrays.asList("private static iterable method (1/2)",
				"private static iterable method (2/2)").iterator();
		}

		@DataPoints
		public static Iterable<String> publicStaticIterableMethod() {
			//Note: This conversion is important because it prevents the return
			//value from being an static of Collection, which has special handling
			return () -> Arrays.asList("public static iterable method (1/2)",
				"public static iterable method (2/2)").iterator();
		}

		@DataPoints
		private static Iterable<String> privateStaticIterableMethod() {
			//Note: This conversion is important because it prevents the return
			//value from being an static of Collection, which has special handling
			return () -> Arrays.asList("private static iterable method (1/2)",
				"private static iterable method (2/2)").iterator();
		}

		public List<String> getExpectedValues() {
			List<String> expectedValues = new ArrayList<>();

			expectedValues.add(PUBLIC_STATIC_STRING_FIELD);
			expectedValues.add(PRIVATE_STATIC_STRING_FIELD);
			expectedValues.addAll(Arrays.asList(PUBLIC_STATIC_ARRAY_FIELD));
			expectedValues.addAll(Arrays.asList(PRIVATE_STATIC_ARRAY_FIELD));
			expectedValues.addAll(PUBLIC_STATIC_LIST_FIELD);
			expectedValues.addAll(PRIVATE_STATIC_LIST_FIELD);

			expectedValues.add(publicStaticStringMethod());
			expectedValues.add(privateStaticStringMethod());
			expectedValues.addAll(Arrays.asList(publicStaticArrayMethod()));
			expectedValues.addAll(Arrays.asList(privateStaticArrayMethod()));
			expectedValues.addAll(publicStaticListMethod());
			expectedValues.addAll(privateStaticListMethod());
			expectedValues.addAll(publicStaticStreamMethod().collect(toList()));
			expectedValues.addAll(privateStaticStreamMethod().collect(toList()));
			copyFromIteratorToList(publicStaticIteratorMethod(), expectedValues);
			copyFromIteratorToList(privateStaticIteratorMethod(), expectedValues);
			copyFromIteratorToList(publicStaticIterableMethod().iterator(), expectedValues);
			copyFromIteratorToList(privateStaticIterableMethod().iterator(), expectedValues);

			return expectedValues;
		}

		private <T> void copyFromIteratorToList(Iterator<T> iterator, List<T> list) {
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}
	}

	private static class ClassWithDataPointMethodThatThrowsException {
		public static final String EXCEPTION_MESSAGE = "Test exception";

		@DataPoint
		public static String throwException() {
			throw new RuntimeException(EXCEPTION_MESSAGE);
		}
	}

	private static class ClassWithDataPointsOfInvalidGroupType {
		@DataPoints
		public static String THIS_IS_NOT_VALID = "Because string is not a valid data points group type";
	}

	private static class ClassWithInvalidStreamField {
		@DataPoints
		public static Stream<String> THIS_IS_NOT_VALID = Stream.of("Because a field",
			"cannot specify data points with a stream");
	}

	private static class ClassWithInvalidIteratorField {
		@DataPoints
		public static Iterator<String> THIS_IS_NOT_VALID = Arrays.asList("Because a field",
			"cannot specify data points with an iterator").iterator();
	}

}
