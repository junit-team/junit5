/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import java.lang.reflect.Parameter;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatter {

	private final String pattern;
	private final String displayName;
	private final Parameter[] parameters;
	private final boolean hasAggregator;

	ParameterizedTestNameFormatter(String pattern, String displayName, Parameter[] parameters, boolean hasAggregator) {
		this.pattern = pattern;
		this.displayName = displayName;
		this.parameters = parameters;
		this.hasAggregator = hasAggregator;
	}

	String format(int invocationIndex, Object... arguments) {
		try {
			return formatSafely(invocationIndex, arguments);
		}
		catch (Exception ex) {
			String message = "The display name pattern defined for the parameterized test is invalid. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

	private String formatSafely(int invocationIndex, Object[] arguments) {
		String pattern = prepareMessageFormatPattern(invocationIndex, arguments.length);
		MessageFormat format = new MessageFormat(pattern);
		Object[] humanReadableArguments = makeReadable(format, arguments);
		return format.format(humanReadableArguments);
	}

	private String prepareMessageFormatPattern(int invocationIndex, int numberOfArguments) {
		String result = pattern//
				.replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)//
				.replace(INDEX_PLACEHOLDER, String.valueOf(invocationIndex));

		if (result.contains(ARGUMENTS_WITH_NAMES_PLACEHOLDER)) {
			result = result.replace(ARGUMENTS_WITH_NAMES_PLACEHOLDER, argumentsWithNamesPattern(numberOfArguments));
		}

		if (result.contains(ARGUMENTS_PLACEHOLDER)) {
			result = result.replace(ARGUMENTS_PLACEHOLDER, argumentsPattern(numberOfArguments));
		}

		return result;
	}

	private String argumentsWithNamesPattern(int numberOfArguments) {
		if (!hasAggregator && areNamesPresent(parameters)) {
			return IntStream.range(0, numberOfArguments) //
					.mapToObj(index -> parameters[index].getName() + "={" + index + "}") //
					.collect(joining(", "));
		}
		return ARGUMENTS_PLACEHOLDER;
	}

	private boolean areNamesPresent(Parameter[] parameters) {
		return parameters.length > 0 && Arrays.stream(parameters).allMatch(Parameter::isNamePresent);
	}

	private String argumentsPattern(int numberOfArguments) {
		return IntStream.range(0, numberOfArguments) //
				.mapToObj(index -> "{" + index + "}") //
				.collect(joining(", "));
	}

	private Object[] makeReadable(MessageFormat format, Object[] arguments) {
		Format[] formats = format.getFormatsByArgumentIndex();
		Object[] result = Arrays.copyOf(arguments, Math.min(arguments.length, formats.length), Object[].class);
		for (int i = 0; i < result.length; i++) {
			if (formats[i] == null) {
				result[i] = StringUtils.nullSafeToString(arguments[i]);
			}
		}
		return result;
	}

}
