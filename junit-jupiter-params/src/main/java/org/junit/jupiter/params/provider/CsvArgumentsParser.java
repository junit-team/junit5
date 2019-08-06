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

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.platform.commons.PreconditionViolationException;

public class CsvArgumentsParser {

	private static final String DEFAULT_DELIMITER = ",";
	private static final String LINE_SEPARATOR = "\n";
	private static final char SINGLE_QUOTE = '\'';
	private static final char DOUBLE_QUOTE = '"';
	private static final char EMPTY_CHAR = '\0';

	private String delimiter;
	private String lineSeparator;
	private char quote;
	private String emptyValue;

	private CsvArgumentsParser(String delimiter, String lineSeparator, char quote, String emptyValue) {
		this.delimiter = delimiter;
		this.lineSeparator = lineSeparator;
		this.quote = quote;
		this.emptyValue = emptyValue;
	}

	public static CsvArgumentsParser from(CsvSource annotation) {
		String delimiter = selectDelimiter(annotation, annotation.delimiter(), annotation.delimiterString());
		return new CsvArgumentsParser(delimiter, LINE_SEPARATOR, SINGLE_QUOTE, annotation.emptyValue());
	}

	public static CsvArgumentsParser from(CsvFileSource annotation) {
		String delimiter = selectDelimiter(annotation, annotation.delimiter(), annotation.delimiterString());
		return new CsvArgumentsParser(delimiter, annotation.lineSeparator(), DOUBLE_QUOTE, annotation.emptyValue());
	}

	private static String selectDelimiter(Annotation annotation, char delimiter, String delimiterString) {
		if (delimiter != EMPTY_CHAR && !delimiterString.isEmpty()) {
			throw new PreconditionViolationException(
				"delimiter and delimiterString cannot be simultaneously set in " + annotation);
		}
		if (delimiter != EMPTY_CHAR) {
			return String.valueOf(delimiter);
		}
		if (!delimiterString.isEmpty()) {
			return delimiterString;
		}
		return DEFAULT_DELIMITER;
	}

	CsvParser getParser() {
		CsvParserSettings csvParserSettings = buildParserSettings();
		return new CsvParser(csvParserSettings);
	}

	private CsvParserSettings buildParserSettings() {
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
