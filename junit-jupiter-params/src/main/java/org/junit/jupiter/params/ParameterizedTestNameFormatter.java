/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

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

	ParameterizedTestNameFormatter(String pattern, String displayName) {
		this.pattern = pattern;
		this.displayName = displayName;
	}

	String format(int invocationIndex, Object... arguments) {
		String pattern = prepareMessageFormatPattern(invocationIndex, arguments);
		Object[] humanReadableArguments = makeReadable(arguments);
		return formatSafely(pattern, humanReadableArguments);
	}

	private String prepareMessageFormatPattern(int invocationIndex, Object[] arguments) {
		String result = pattern//
				.replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)//
				.replace(INDEX_PLACEHOLDER, String.valueOf(invocationIndex));

		if (result.contains(ARGUMENTS_PLACEHOLDER)) {
			// @formatter:off
			String replacement = IntStream.range(0, arguments.length)
					.mapToObj(index -> "{" + index + "}")
					.collect(joining(", "));
			// @formatter:on
			result = result.replace(ARGUMENTS_PLACEHOLDER, replacement);
		}

		return result;
	}

	private Object[] makeReadable(Object[] arguments) {
		// Note: humanReadableArguments must be an Object[] in order to
		// avoid varargs issues with non-Eclipse compilers.
		Object[] humanReadableArguments = //
			Arrays.stream(arguments).map(StringUtils::nullSafeToString).toArray(String[]::new);
		return humanReadableArguments;
	}

	private String formatSafely(String pattern, Object[] arguments) {
		try {
			return MessageFormat.format(pattern, arguments);
		}
		catch (IllegalArgumentException ex) {
			String message = "The display name pattern defined for the parameterized test is invalid. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

}
