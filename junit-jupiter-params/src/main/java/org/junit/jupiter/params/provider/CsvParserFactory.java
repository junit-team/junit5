/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.lang.annotation.Annotation;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.6
 */
class CsvParserFactory {

	private static final String DEFAULT_DELIMITER = ",";
	private static final String LINE_SEPARATOR = "\n";
	private static final char SINGLE_QUOTE = '\'';
	private static final char DOUBLE_QUOTE = '"';
	private static final char EMPTY_CHAR = '\0';

	static CsvParser createParserFor(CsvSource annotation) {
		String delimiter = selectDelimiter(annotation, annotation.delimiter(), annotation.delimiterString());
		return createParser(delimiter, LINE_SEPARATOR, SINGLE_QUOTE, annotation.emptyValue());
	}

	static CsvParser createParserFor(CsvFileSource annotation) {
		String delimiter = selectDelimiter(annotation, annotation.delimiter(), annotation.delimiterString());
		return createParser(delimiter, annotation.lineSeparator(), DOUBLE_QUOTE, annotation.emptyValue());
	}

	private static String selectDelimiter(Annotation annotation, char delimiter, String delimiterString) {
		Preconditions.condition(delimiter == EMPTY_CHAR || delimiterString.isEmpty(),
			() -> "The delimiter and delimiterString attributes cannot be set simultaneously in " + annotation);

		if (delimiter != EMPTY_CHAR) {
			return String.valueOf(delimiter);
		}
		if (!delimiterString.isEmpty()) {
			return delimiterString;
		}
		return DEFAULT_DELIMITER;
	}

	private static CsvParser createParser(String delimiter, String lineSeparator, char quote, String emptyValue) {
		return new CsvParser(createParserSettings(delimiter, lineSeparator, quote, emptyValue));
	}

	private static CsvParserSettings createParserSettings(String delimiter, String lineSeparator, char quote,
			String emptyValue) {

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(delimiter);
		settings.getFormat().setLineSeparator(lineSeparator);
		settings.getFormat().setQuote(quote);
		settings.getFormat().setQuoteEscape(quote);
		settings.setEmptyValue(emptyValue);
		settings.setAutoConfigurationEnabled(false);
		// Do not use the built-in support for skipping rows/lines since it will
		// throw an IllegalArgumentException if the file does not contain at least
		// the number of specified lines to skip.
		// settings.setNumberOfRowsToSkip(annotation.numLinesToSkip());
		return settings;
	}

}
