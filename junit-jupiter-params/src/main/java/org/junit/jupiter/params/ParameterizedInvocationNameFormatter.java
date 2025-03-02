/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.ARGUMENT_SET_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.INDEX_PLACEHOLDER;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class ParameterizedInvocationNameFormatter {

	static final String DEFAULT_DISPLAY_NAME = "{default_display_name}";
	static final String DEFAULT_DISPLAY_NAME_PATTERN = "[" + INDEX_PLACEHOLDER + "] "
			+ ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
	static final String DISPLAY_NAME_PATTERN_KEY = "junit.jupiter.params.displayname.default";
	static final String ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength";

	static ParameterizedInvocationNameFormatter create(ExtensionContext extensionContext,
			ParameterizedDeclarationContext<?> declarationContext) {

		String name = declarationContext.getDisplayNamePattern();
		String pattern = name.equals(DEFAULT_DISPLAY_NAME)
				? extensionContext.getConfigurationParameter(DISPLAY_NAME_PATTERN_KEY) //
						.orElse(DEFAULT_DISPLAY_NAME_PATTERN)
				: name;
		pattern = Preconditions.notBlank(pattern.trim(), () -> String.format(
			"Configuration error: @%s on %s must be declared with a non-empty name.",
			declarationContext.getAnnotationName(),
			declarationContext.getResolverFacade().getIndexedParameterDeclarations().getSourceElementDescription()));

		int argumentMaxLength = extensionContext.getConfigurationParameter(ARGUMENT_MAX_LENGTH_KEY, Integer::parseInt) //
				.orElse(512);

		return new ParameterizedInvocationNameFormatter(pattern, extensionContext.getDisplayName(), declarationContext,
			argumentMaxLength);
	}

	private final PartialFormatter[] partialFormatters;

	ParameterizedInvocationNameFormatter(String pattern, String displayName,
			ParameterizedDeclarationContext<?> declarationContext, int argumentMaxLength) {
		try {
			this.partialFormatters = parse(pattern, displayName, declarationContext, argumentMaxLength);
		}
		catch (Exception ex) {
			String message = "The display name pattern defined for the parameterized test is invalid. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

	String format(int invocationIndex, EvaluatedArgumentSet arguments) {
		try {
			return formatSafely(invocationIndex, arguments);
		}
		catch (Exception ex) {
			String message = "Failed to format display name for parameterized test. "
					+ "See nested exception for further details.";
			throw new JUnitException(message, ex);
		}
	}

	private String formatSafely(int invocationIndex, EvaluatedArgumentSet arguments) {
		ArgumentsContext context = new ArgumentsContext(invocationIndex, arguments.getConsumedNames(),
			arguments.getName());
		StringBuffer result = new StringBuffer(); // used instead of StringBuilder so MessageFormat can append directly
		for (PartialFormatter partialFormatter : this.partialFormatters) {
			partialFormatter.append(context, result);
		}
		return result.toString();
	}

	private PartialFormatter[] parse(String pattern, String displayName,
			ParameterizedDeclarationContext<?> declarationContext, int argumentMaxLength) {

		List<PartialFormatter> result = new ArrayList<>();
		PartialFormatters formatters = createPartialFormatters(displayName, declarationContext, argumentMaxLength);
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

	private PartialFormatters createPartialFormatters(String displayName,
			ParameterizedDeclarationContext<?> declarationContext, int argumentMaxLength) {

		PartialFormatter argumentsWithNamesFormatter = new CachingByArgumentsLengthPartialFormatter(
			length -> new MessageFormatPartialFormatter(argumentsWithNamesPattern(length, declarationContext),
				argumentMaxLength));

		PartialFormatter argumentSetNameFormatter = new ArgumentSetNameFormatter(
			declarationContext.getAnnotationName());

		PartialFormatters formatters = new PartialFormatters();
		formatters.put(INDEX_PLACEHOLDER, PartialFormatter.INDEX);
		formatters.put(DISPLAY_NAME_PLACEHOLDER, (context, result) -> result.append(displayName));
		formatters.put(ARGUMENT_SET_NAME_PLACEHOLDER, argumentSetNameFormatter);
		formatters.put(ARGUMENTS_WITH_NAMES_PLACEHOLDER, argumentsWithNamesFormatter);
		formatters.put(ARGUMENTS_PLACEHOLDER, new CachingByArgumentsLengthPartialFormatter(
			length -> new MessageFormatPartialFormatter(argumentsPattern(length), argumentMaxLength)));
		formatters.put(ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER, (context, result) -> {
			PartialFormatter formatterToUse = context.argumentSetName.isPresent() //
					? argumentSetNameFormatter //
					: argumentsWithNamesFormatter;
			formatterToUse.append(context, result);
		});
		return formatters;
	}

	private static String argumentsWithNamesPattern(int length, ParameterizedDeclarationContext<?> declarationContext) {
		ResolverFacade resolverFacade = declarationContext.getResolverFacade();
		return IntStream.range(0, length) //
				.mapToObj(index -> resolverFacade.getParameterName(index).map(name -> name + "=").orElse("") + "{"
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
		private final Object[] consumedArguments;
		private final Optional<String> argumentSetName;

		ArgumentsContext(int invocationIndex, Object[] consumedArguments, Optional<String> argumentSetName) {
			this.invocationIndex = invocationIndex;
			this.consumedArguments = consumedArguments;
			this.argumentSetName = argumentSetName;
		}
	}

	@FunctionalInterface
	private interface PartialFormatter {

		PartialFormatter INDEX = (context, result) -> result.append(context.invocationIndex);

		void append(ArgumentsContext context, StringBuffer result);

	}

	private static class ArgumentSetNameFormatter implements PartialFormatter {

		private final String annotationName;

		ArgumentSetNameFormatter(String annotationName) {
			this.annotationName = annotationName;
		}

		@Override
		public void append(ArgumentsContext context, StringBuffer result) {
			if (context.argumentSetName.isPresent()) {
				result.append(context.argumentSetName.get());
				return;
			}
			throw new ExtensionConfigurationException(String.format(
				"When the display name pattern for a @%s contains %s, the arguments must be supplied as an ArgumentSet.",
				this.annotationName, ARGUMENT_SET_NAME_PLACEHOLDER));
		}
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
