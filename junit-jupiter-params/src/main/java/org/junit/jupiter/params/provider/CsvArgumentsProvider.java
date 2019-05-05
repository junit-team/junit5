/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

	private static final String LINE_SEPARATOR = "\n";

	private CsvSource annotation;

	@Override
	public void accept(CsvSource annotation) {
		this.annotation = annotation;
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(this.annotation.delimiter());
		settings.getFormat().setLineSeparator(LINE_SEPARATOR);
		settings.getFormat().setQuote('\'');
		settings.getFormat().setQuoteEscape('\'');
		settings.setEmptyValue(this.annotation.emptyValue());
		settings.setAutoConfigurationEnabled(false);
		CsvParser csvParser = new CsvParser(settings);
		AtomicLong index = new AtomicLong(0);

		// @formatter:off
		return Arrays.stream(this.annotation.value())
				.map(line -> {
					String[] parsedLine = null;
					try {
						parsedLine = csvParser.parseLine(line + LINE_SEPARATOR);
					}
					catch (Throwable throwable) {
						handleCsvException(throwable, this.annotation);
					}
					Preconditions.notNull(parsedLine,
						() -> "Line at index " + index.get() + " contains invalid CSV: \"" + line + "\"");
					return parsedLine;
				})
				.peek(values -> index.incrementAndGet())
				.map(Arguments::of);
		// @formatter:on
	}

	static void handleCsvException(Throwable throwable, Annotation annotation) {
		BlacklistedExceptions.rethrowIfBlacklisted(throwable);
		if (throwable instanceof PreconditionViolationException) {
			throw (PreconditionViolationException) throwable;
		}
		throw new CsvParsingException("Failed to parse CSV input configured via " + annotation, throwable);
	}

}
