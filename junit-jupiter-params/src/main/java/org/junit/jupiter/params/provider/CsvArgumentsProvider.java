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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

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

	@Override
	protected Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context,
			CsvSource csvSource) {

		CsvReaderFactory.validate(csvSource);

		List<Arguments> arguments = new ArrayList<>();

		try (var reader = CsvReaderFactory.createReaderFor(csvSource, getData(csvSource))) {
			boolean useHeadersInDisplayName = csvSource.useHeadersInDisplayName();
			for (CsvRecord record : reader) {
				arguments.add(processCsvRecord(record, useHeadersInDisplayName));
			}
		}
		catch (Throwable throwable) {
			throw handleCsvException(throwable, csvSource);
		}

		return arguments.stream();
	}

	private static String getData(CsvSource csvSource) {
		Preconditions.condition(csvSource.value().length > 0 ^ !csvSource.textBlock().isEmpty(),
			() -> "@CsvSource must be declared with either `value` or `textBlock` but not both");

		return csvSource.value().length > 0 ? String.join("\n", csvSource.value()) : csvSource.textBlock();
	}

	/**
	 * Processes custom null values, supports wrapping of column values in
	 * {@link Named} if necessary (for CSV header support), and returns the
	 * CSV record wrapped in an {@link Arguments} instance.
	 */
	static Arguments processCsvRecord(CsvRecord record, boolean useHeadersInDisplayName) {
		List<String> fields = record.getFields();
		List<String> headers = useHeadersInDisplayName ? getHeaders(record) : List.of();

		Preconditions.condition(!useHeadersInDisplayName || fields.size() <= headers.size(),
			() -> String.format( //
				"The number of columns (%d) exceeds the number of supplied headers (%d) in CSV record: %s", //
				fields.size(), headers.size(), fields)); //

		@Nullable
		Object[] arguments = new Object[fields.size()];

		for (int i = 0; i < fields.size(); i++) {
			Object argument = resolveNullMarker(fields.get(i));
			if (useHeadersInDisplayName) {
				String header = resolveNullMarker(headers.get(i));
				argument = asNamed(header + " = " + argument, argument);
			}
			arguments[i] = argument;
		}

		return Arguments.of(arguments);
	}

	private static List<String> getHeaders(CsvRecord record) {
		return ((NamedCsvRecord) record).getHeader();
	}

	private static @Nullable String resolveNullMarker(String record) {
		return CsvReaderFactory.DefaultFieldModifier.NULL_MARKER.equals(record) ? null : record;
	}

	private static Named<@Nullable Object> asNamed(String name, @Nullable Object column) {
		return Named.<@Nullable Object> of(name, column);
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
