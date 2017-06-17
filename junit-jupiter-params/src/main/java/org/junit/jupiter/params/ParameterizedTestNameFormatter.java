/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.joining;

import java.text.MessageFormat;
import java.util.stream.IntStream;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatter {

	private final String namePattern;

	ParameterizedTestNameFormatter(String namePattern) {
		this.namePattern = namePattern;
	}

	String format(int invocationIndex, Object... arguments) {
		String pattern = prepareMessageFormatPattern(invocationIndex, arguments);
		Object[] humanReadableArguments = makeReadable(arguments);
		return formatSafely(pattern, humanReadableArguments);
	}

	private String prepareMessageFormatPattern(int invocationIndex, Object[] arguments) {
		String result = namePattern.replace("{index}", String.valueOf(invocationIndex));
		if (result.contains("{arguments}")) {
			// @formatter:off
			String replacement = IntStream.range(0, arguments.length)
					.mapToObj(index -> "{" + index + "}")
					.collect(joining(", "));
			// @formatter:on
			result = result.replace("{arguments}", replacement);
		}
		return result;
	}

	private Object[] makeReadable(Object[] arguments) {
		// Note: humanReadableArguments must be an Object[] in order to
		// avoid varargs issues with non-Eclipse compilers.
		Object[] humanReadableArguments = new String[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			humanReadableArguments[i] = StringUtils.nullSafeToString(arguments[i]);
		}
		return humanReadableArguments;
	}

	private String formatSafely(String pattern, Object[] arguments) {
		try {
			return MessageFormat.format(pattern, arguments);
		}
		catch (IllegalArgumentException ex) {
			String message = "The naming pattern defined for the parameterized tests is invalid. "
					+ "The nested exception contains more details.";
			throw new JUnitException(message, ex);
		}
	}

}
