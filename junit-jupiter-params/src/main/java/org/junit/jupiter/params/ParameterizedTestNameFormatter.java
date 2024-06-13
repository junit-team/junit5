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

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENT_SET_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.Arguments.ArgumentSet;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatter {

	private final PartialFormatter[] partialFormatters;

	ParameterizedTestNameFormatter(String pattern, String displayName, ParameterizedTestMethodContext methodContext,
			int argumentMaxLength) {
		try {
			this.partialFormatters = parse(pattern, displayName, methodContext, argumentMaxLength);
		}
		catch (Exception ex) {
			String message = "The display name pattern defined for the parameterized test is invalid. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

	String format(int invocationIndex, Arguments arguments, Object[] consumedArguments) {
		try {
			return formatSafely(invocationIndex, arguments, consumedArguments);
		}
		catch (Exception ex) {
			String message = "Failed to format display name for parameterized test. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

	private String formatSafely(int invocationIndex, Arguments arguments, Object[] consumedArguments) {
		ArgumentsContext context = new ArgumentsContext(invocationIndex, arguments,
			extractNamedArguments(consumedArguments));
		StringBuffer result = new StringBuffer(); // used instead of StringBuilder so MessageFormat can append directly
		for (PartialFormatter partialFormatter : this.partialFormatters) {
			partialFormatter.append(context, result);
		}
		return result.toString();
	}

	private Object[] extractNamedArguments(Object[] arguments) {
		return Arrays.stream(arguments) //
				.map(argument -> argument instanceof Named ? ((Named<?>) argument).getName() : argument) //
				.toArray();
	}

	private PartialFormatter[] parse(String pattern, String displayName, ParameterizedTestMethodContext methodContext,
			int argumentMaxLength) {

		List<PartialFormatter> result = new ArrayList<>();
		PartialFormatters formatters = createPartialFormatters(displayName, methodContext, argumentMaxLength);
		String unparsedSegment = pattern;

		while (isNotBlank(unparsedSegment)) {
			PlaceholderPosition position = findFirstPlaceholder(formatters, unparsedSegment);
			if (position == null) {
				result.add(determineNonPlaceholderFormatter(unparsedSegment, argumentMaxLength));
				break;
			}
			if (position.index > 0) {
				String before = unparsedSegment.substring(0, position.index);
				result.add(determineNonPlaceholderFormatter(before, argumentMaxLength));
			}
			result.add(formatters.get(position.placeholder));
			unparsedSegment = unparsedSegment.substring(position.index + position.placeholder.length());
		}

		return result.toArray(new PartialFormatter[0]);
	}

	private static PlaceholderPosition findFirstPlaceholder(PartialFormatters formatters, String segment) {
		if (segment.length() < formatters.minimumPlaceholderLength) {
			return null;
		}
		PlaceholderPosition minimum = null;
		for (String placeholder : formatters.placeholders()) {
			int index = segment.indexOf(placeholder);
			if (index >= 0) {
				if (index < formatters.minimumPlaceholderLength) {
					return new PlaceholderPosition(index, placeholder);
				}
				else if (minimum == null || index < minimum.index) {
					minimum = new PlaceholderPosition(index, placeholder);
				}
			}
		}
		return minimum;
	}

	private static PartialFormatter determineNonPlaceholderFormatter(String segment, int argumentMaxLength) {
		return segment.contains("{") //
				? new MessageFormatPartialFormatter(segment, argumentMaxLength) //
				: (context, result) -> result.append(segment);
	}

	private PartialFormatters createPartialFormatters(String displayName, ParameterizedTestMethodContext methodContext,
			int argumentMaxLength) {

		PartialFormatter argumentsWithNamesFormatter = new CachingByArgumentsLengthPartialFormatter(
			length -> new MessageFormatPartialFormatter(argumentsWithNamesPattern(length, methodContext),
				argumentMaxLength));

		PartialFormatters formatters = new PartialFormatters();
		formatters.put(INDEX_PLACEHOLDER, PartialFormatter.INDEX);
		formatters.put(DISPLAY_NAME_PLACEHOLDER, (context, result) -> result.append(displayName));
		formatters.put(ARGUMENT_SET_NAME_PLACEHOLDER, PartialFormatter.ARGUMENT_SET_NAME);
		formatters.put(ARGUMENTS_WITH_NAMES_PLACEHOLDER, argumentsWithNamesFormatter);
		formatters.put(ARGUMENTS_PLACEHOLDER, new CachingByArgumentsLengthPartialFormatter(
			length -> new MessageFormatPartialFormatter(argumentsPattern(length), argumentMaxLength)));
		formatters.put(ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER, (context, result) -> {
			PartialFormatter formatterToUse = context.arguments instanceof ArgumentSet //
					? PartialFormatter.ARGUMENT_SET_NAME //
					: argumentsWithNamesFormatter;
			formatterToUse.append(context, result);
		});
		return formatters;
	}

	private static String argumentsWithNamesPattern(int length, ParameterizedTestMethodContext methodContext) {
		return IntStream.range(0, length) //
				.mapToObj(index -> methodContext.getParameterName(index).map(name -> name + "=").orElse("") + "{"
						+ index + "}") //
				.collect(joining(", "));
	}

	private static String argumentsPattern(int length) {
		return IntStream.range(0, length) //
				.mapToObj(index -> "{" + index + "}") //
				.collect(joining(", "));
	}

	private static class PlaceholderPosition {

		final int index;
		final String placeholder;

		PlaceholderPosition(int index, String placeholder) {
			this.index = index;
			this.placeholder = placeholder;
		}

	}

	private static class ArgumentsContext {

		private final int invocationIndex;
		private final Arguments arguments;
		private final Object[] consumedArguments;

		ArgumentsContext(int invocationIndex, Arguments arguments, Object[] consumedArguments) {
			this.invocationIndex = invocationIndex;
			this.arguments = arguments;
			this.consumedArguments = consumedArguments;
		}
	}

	@FunctionalInterface
	private interface PartialFormatter {

		PartialFormatter INDEX = (context, result) -> result.append(context.invocationIndex);

		PartialFormatter ARGUMENT_SET_NAME = (context, result) -> {
			if (!(context.arguments instanceof ArgumentSet)) {
				throw new ExtensionConfigurationException(
					String.format("When the display name pattern for a @ParameterizedTest contains %s, "
							+ "the arguments must be supplied as an ArgumentSet.",
						ARGUMENT_SET_NAME_PLACEHOLDER));
			}
			result.append(((ArgumentSet) context.arguments).getName());
		};

		void append(ArgumentsContext context, StringBuffer result);
	}

	private static class MessageFormatPartialFormatter implements PartialFormatter {

		@SuppressWarnings("UnnecessaryUnicodeEscape")
		private static final char ELLIPSIS = '\u2026';

		private final MessageFormat messageFormat;
		private final int argumentMaxLength;

		MessageFormatPartialFormatter(String pattern, int argumentMaxLength) {
			this.messageFormat = new MessageFormat(pattern);
			this.argumentMaxLength = argumentMaxLength;
		}

		// synchronized because MessageFormat is not thread-safe
		@Override
		public synchronized void append(ArgumentsContext context, StringBuffer result) {
			this.messageFormat.format(makeReadable(context.consumedArguments), result, new FieldPosition(0));
		}

		private Object[] makeReadable(Object[] arguments) {
			Format[] formats = messageFormat.getFormatsByArgumentIndex();
			Object[] result = Arrays.copyOf(arguments, Math.min(arguments.length, formats.length), Object[].class);
			for (int i = 0; i < result.length; i++) {
				if (formats[i] == null) {
					result[i] = truncateIfExceedsMaxLength(StringUtils.nullSafeToString(arguments[i]));
				}
			}
			return result;
		}

		private String truncateIfExceedsMaxLength(String argument) {
			if (argument != null && argument.length() > this.argumentMaxLength) {
				return argument.substring(0, this.argumentMaxLength - 1) + ELLIPSIS;
			}
			return argument;
		}
	}

	private static class CachingByArgumentsLengthPartialFormatter implements PartialFormatter {

		private final ConcurrentMap<Integer, PartialFormatter> cache = new ConcurrentHashMap<>(1);
		private final Function<Integer, PartialFormatter> factory;

		CachingByArgumentsLengthPartialFormatter(Function<Integer, PartialFormatter> factory) {
			this.factory = factory;
		}

		@Override
		public void append(ArgumentsContext context, StringBuffer result) {
			cache.computeIfAbsent(context.consumedArguments.length, factory).append(context, result);
		}
	}

	private static class PartialFormatters {

		private final Map<String, PartialFormatter> formattersByPlaceholder = new LinkedHashMap<>();
		private int minimumPlaceholderLength = Integer.MAX_VALUE;

		void put(String placeholder, PartialFormatter formatter) {
			formattersByPlaceholder.put(placeholder, formatter);
			int newPlaceholderLength = placeholder.length();
			if (newPlaceholderLength < minimumPlaceholderLength) {
				minimumPlaceholderLength = newPlaceholderLength;
			}
		}

		PartialFormatter get(String placeholder) {
			return formattersByPlaceholder.get(placeholder);
		}

		Set<String> placeholders() {
			return formattersByPlaceholder.keySet();
		}
	}

}
