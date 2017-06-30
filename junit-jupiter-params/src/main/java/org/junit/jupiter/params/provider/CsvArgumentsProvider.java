/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import java.util.Arrays;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

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
		settings.getFormat().setQuote('\'');
		settings.getFormat().setQuoteEscape('\'');
		settings.setAutoConfigurationEnabled(false);
		CsvParser csvParser = new CsvParser(settings);
		return Arrays.stream(lines).map(csvParser::parseLine).map(Arguments::of);
	}

}
