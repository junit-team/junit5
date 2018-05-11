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
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.domain.TheoryParameterDetails;

/**
 * Utility class for creating data points for well-known types.
 */
@API(status = INTERNAL, since = "5.3")
public class WellKnownTypesUtils {
	/**
	 * Possible data points for a boolean theory parameter.
	 */
	private static final List<DataPointDetails> BOOLEAN_DATA_POINT_DETAILS = Arrays.asList(
		new DataPointDetails(false, Collections.emptyList(), "Automatic boolean data point generation"),
		new DataPointDetails(true, Collections.emptyList(), "Automatic boolean data point generation"));

	/**
	 * Builds data points for a theory parameter that accepts an enum.
	 *
	 * @param theoryParameterDetails the details of the parameter to build data
	 * points for
	 * @return the constructed data points
	 */
	private static List<DataPointDetails> buildDataPointDetailsFromEnumValues(
			TheoryParameterDetails theoryParameterDetails) {
		Object[] enumValues = theoryParameterDetails.getType().getEnumConstants();
		return Stream.of(enumValues).map(v -> new DataPointDetails(v, Collections.<String> emptyList(),
			"Automatic enum data point generation")).collect(toList());
	}

	/**
	 * Determines if the provided type is a well-known type.
	 *
	 * @param typeToTest the type to test. Cannot be a primitive
	 * @return {@code true} if the provided type is a well-known type (i.e. if
	 * it can be used to produce all possible data points without any further
	 * input); {@code false} otherwise
	 */
	public boolean isKnownType(Class<?> typeToTest) {
		if (typeToTest.isPrimitive()) {
			throw new IllegalArgumentException("isKnownType does not accept primitives");
		}

		// @formatter:off
		return Arrays.stream(SupportedTypes.values())
				.map(v -> v.isSupportedPredicate)
				.anyMatch(v -> v.test(typeToTest));
		// @formatter:on
	}

	/**
	 * Generates the data point details for the provided type.
	 *
	 * @param parameterDetails the parameter to build data points for
	 * @return an {@link Optional} containing the {@link List} of constructed
	 * data points
	 */
	public Optional<List<DataPointDetails>> getDataPointDetails(TheoryParameterDetails parameterDetails) {
		// @formatter:off
		return Arrays.stream(SupportedTypes.values())
				.filter(v -> v.isSupportedPredicate.test(parameterDetails.getNonPrimitiveType()))
				.findAny()
				.map(v -> v.dataPointDetailsFactory.apply(parameterDetails));
		// @formatter:on
	}

	/**
	 * Enumeration of supported types.
	 */
	private enum SupportedTypes {
		BOOLEAN(v -> v == Boolean.class, ignored -> BOOLEAN_DATA_POINT_DETAILS),
		ENUM(Enum.class::isAssignableFrom, WellKnownTypesUtils::buildDataPointDetailsFromEnumValues);

		private final Predicate<Class<?>> isSupportedPredicate;
		private final Function<TheoryParameterDetails, List<DataPointDetails>> dataPointDetailsFactory;

		/**
		 * Constructor.
		 *
		 * @param isSupportedPredicate the predicate that determines if a type
		 * is supported
		 * @param dataPointDetailsFactory the factory that builds data points
		 * for a provided parameter
		 */
		SupportedTypes(Predicate<Class<?>> isSupportedPredicate,
				Function<TheoryParameterDetails, List<DataPointDetails>> dataPointDetailsFactory) {

			this.isSupportedPredicate = isSupportedPredicate;
			this.dataPointDetailsFactory = dataPointDetailsFactory;
		}
	}
}
