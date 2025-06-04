/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.params.provider.CsvParserFactory.createParserFor;

import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider extends AnnotationBasedArgumentsProvider<CsvSource> {

	private static final String LINE_SEPARATOR = "\n";

	@Override
	protected Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context,
			CsvSource csvSource) {
		Set<String> nullValues = Set.of(csvSource.nullValues());
		CsvParser csvParser = createParserFor(csvSource);
		final boolean textBlockDeclared = !csvSource.textBlock().isEmpty();
		Preconditions.condition(csvSource.value().length > 0 ^ textBlockDeclared,
			() -> "@CsvSource must be declared with either `value` or `textBlock` but not both");

		return textBlockDeclared ? parseTextBlock(csvSource, csvParser, nullValues)
				: parseValueArray(csvSource, csvParser, nullValues);
	}

	private Stream<Arguments> parseTextBlock(CsvSource csvSource, CsvParser csvParser, Set<String> nullValues) {
		String textBlock = csvSource.textBlock();
		boolean useHeadersInDisplayName = csvSource.useHeadersInDisplayName();
		List<Arguments> argumentsList = new ArrayList<>();

		try {
			List<@Nullable String[]> csvRecords = parseAll(csvParser, new StringReader(textBlock));
			String[] headers = useHeadersInDisplayName ? getHeaders(csvParser) : null;

			AtomicInteger index = new AtomicInteger(0);
			for (var csvRecord : csvRecords) {
				index.incrementAndGet();
				Preconditions.notNull(csvRecord,
					() -> "Record at index " + index + " contains invalid CSV: \"\"\"\n" + textBlock + "\n\"\"\"");
				argumentsList.add(processCsvRecord(csvRecord, nullValues, useHeadersInDisplayName, headers));
			}
		}
		catch (Throwable throwable) {
			throw handleCsvException(throwable, csvSource);
		}

		return argumentsList.stream();
	}

	@SuppressWarnings("NullAway")
	private static List<@Nullable String[]> parseAll(CsvParser csvParser, Reader reader) {
		return csvParser.parseAll(reader);
	}

	private Stream<Arguments> parseValueArray(CsvSource csvSource, CsvParser csvParser, Set<String> nullValues) {
		boolean useHeadersInDisplayName = csvSource.useHeadersInDisplayName();
		List<Arguments> argumentsList = new ArrayList<>();

		try {
			String[] headers = null;
			AtomicInteger index = new AtomicInteger(0);
			for (String input : csvSource.value()) {
				index.incrementAndGet();
				String[] csvRecord = csvParser.parseLine(input + LINE_SEPARATOR);
				// Lazily retrieve headers if necessary.
				if (useHeadersInDisplayName && headers == null) {
					headers = getHeaders(csvParser);
					continue;
				}
				Preconditions.notNull(csvRecord,
					() -> "Record at index " + index + " contains invalid CSV: \"" + input + "\"");
				argumentsList.add(processCsvRecord(csvRecord, nullValues, useHeadersInDisplayName, headers));
			}
		}
		catch (Throwable throwable) {
			throw handleCsvException(throwable, csvSource);
		}

		return argumentsList.stream();
	}

	// Cannot get parsed headers until after parsing has started.
	static String[] getHeaders(CsvParser csvParser) {
		return Arrays.stream(csvParser.getContext().parsedHeaders())//
				.map(String::trim)//
				.toArray(String[]::new);
	}

	/**
	 * Processes custom null values, supports wrapping of column values in
	 * {@link Named} if necessary (for CSV header support), and returns the
	 * CSV record wrapped in an {@link Arguments} instance.
	 */
	static Arguments processCsvRecord(@Nullable String[] csvRecord, Set<String> nullValues,
			boolean useHeadersInDisplayName, String @Nullable [] headers) {

		// Nothing to process?
		if (nullValues.isEmpty() && !useHeadersInDisplayName) {
			return Arguments.of((Object[]) csvRecord);
		}

		Preconditions.condition(!useHeadersInDisplayName || (csvRecord.length <= requireNonNull(headers).length),
			() -> "The number of columns (%d) exceeds the number of supplied headers (%d) in CSV record: %s".formatted(
				csvRecord.length, requireNonNull(headers).length, Arrays.toString(csvRecord)));

		@Nullable
		Object[] arguments = new Object[csvRecord.length];
		for (int i = 0; i < csvRecord.length; i++) {
			Object column = csvRecord[i];
			if (column != null && nullValues.contains(column)) {
				column = null;
			}
			if (useHeadersInDisplayName) {
				column = asNamed(requireNonNull(headers)[i] + " = " + column, column);
			}
			arguments[i] = column;
		}
		return Arguments.of(arguments);
	}

	@SuppressWarnings("NullAway")
	private static Named<@Nullable Object> asNamed(String name, @Nullable Object column) {
		return Named.of(name, column);
	}

	/**
	 * @return this method always throws an exception and therefore never
	 * returns anything; the return type is merely present to allow this
	 * method to be supplied as the operand in a {@code throw} statement
	 */
	static RuntimeException handleCsvException(Throwable throwable, Annotation annotation) {
		UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
		if (throwable instanceof PreconditionViolationException exception) {
			throw exception;
		}
		throw new CsvParsingException("Failed to parse CSV input configured via " + annotation, throwable);
	}

}
