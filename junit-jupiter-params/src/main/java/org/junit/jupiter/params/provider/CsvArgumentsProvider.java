/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

	private static final String LINE_SEPARATOR = "\n";

	private String[] lines;
	private char delimiter;

	@Override
	public void accept(CsvSource annotation) {
		lines = annotation.value();
		delimiter = annotation.delimiter();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(delimiter);
		settings.getFormat().setLineSeparator(LINE_SEPARATOR);
		settings.getFormat().setQuote('\'');
		settings.getFormat().setQuoteEscape('\'');
		settings.setEmptyValue("");
		settings.setAutoConfigurationEnabled(false);
		CsvParser csvParser = new CsvParser(settings);
		AtomicLong index = new AtomicLong(0);
		// @formatter:off
		return Arrays.stream(lines)
				.map(
					line -> Preconditions.notNull(csvParser.parseLine(line + LINE_SEPARATOR),
					() -> "Line at index " + index.get() + " contains invalid CSV: \"" + line + "\"")
				)
				.peek(values -> index.incrementAndGet())
				.map(Arguments::of);
		// @formatter:on
	}

}
