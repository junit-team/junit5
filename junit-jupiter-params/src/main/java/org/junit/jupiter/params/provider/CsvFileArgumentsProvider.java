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

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.CsvArgumentsProvider.handleCsvException;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvFileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvFileSource> {

	private final BiFunction<Class<?>, String, InputStream> inputStreamProvider;

	private CsvFileSource annotation;
	private String[] resources;
	private Charset charset;
	private CsvParserSettings settings;
	private int numLinesToSkip;

	CsvFileArgumentsProvider() {
		this(Class::getResourceAsStream);
	}

	CsvFileArgumentsProvider(BiFunction<Class<?>, String, InputStream> inputStreamProvider) {
		this.inputStreamProvider = inputStreamProvider;
	}

	@Override
	public void accept(CsvFileSource annotation) {
		this.annotation = annotation;
		resources = annotation.resources();
		try {
			this.charset = Charset.forName(annotation.encoding());
		}
		catch (Exception ex) {
			throw new PreconditionViolationException("The charset supplied in " + this.annotation + " is invalid", ex);
		}
		numLinesToSkip = annotation.numLinesToSkip();
		settings = new CsvParserSettings();
		// Do not use the built-in support for skipping rows/lines since it will
		// throw an IllegalArgumentException if the file does not contain at least
		// the number of specified lines to skip.
		// settings.setNumberOfRowsToSkip(annotation.numLinesToSkip());
		settings.getFormat().setDelimiter(annotation.delimiter());
		settings.getFormat().setLineSeparator(annotation.lineSeparator());
		settings.getFormat().setQuote('"');
		settings.getFormat().setQuoteEscape('"');
		settings.setEmptyValue(annotation.emptyValue());
		settings.setAutoConfigurationEnabled(false);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		// @formatter:off
		return Arrays.stream(resources)
				.map(resource -> openInputStream(context, resource))
				.map(this::createCsvParser)
				.flatMap(this::toStream);
		// @formatter:on
	}

	private InputStream openInputStream(ExtensionContext context, String resource) {
		Preconditions.notBlank(resource, "Classpath resource [" + resource + "] must not be null or blank");
		Class<?> testClass = context.getRequiredTestClass();
		return Preconditions.notNull(inputStreamProvider.apply(testClass, resource),
			() -> "Classpath resource [" + resource + "] does not exist");
	}

	private CsvParser createCsvParser(InputStream inputStream) {
		CsvParser csvParser = new CsvParser(settings);
		try {
			csvParser.beginParsing(inputStream, charset);
		}
		catch (Throwable throwable) {
			handleCsvException(throwable, this.annotation);
		}
		return csvParser;
	}

	private Stream<Arguments> toStream(CsvParser csvParser) {
		CsvParserIterator iterator = new CsvParserIterator(csvParser, this.annotation);
		return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false) //
				.skip(this.numLinesToSkip) //
				.onClose(() -> {
					try {
						csvParser.stopParsing();
					}
					catch (Throwable throwable) {
						handleCsvException(throwable, this.annotation);
					}
				});
	}

	private static class CsvParserIterator implements Iterator<Arguments> {

		private final CsvParser csvParser;

		private final CsvFileSource annotation;

		private Object[] nextCsvRecord;

		CsvParserIterator(CsvParser csvParser, CsvFileSource annotation) {
			this.csvParser = csvParser;
			this.annotation = annotation;
			advance();
		}

		@Override
		public boolean hasNext() {
			return this.nextCsvRecord != null;
		}

		@Override
		public Arguments next() {
			Arguments result = arguments(this.nextCsvRecord);
			advance();
			return result;
		}

		private void advance() {
			try {
				this.nextCsvRecord = this.csvParser.parseNext();
			}
			catch (Throwable throwable) {
				handleCsvException(throwable, this.annotation);
			}
		}
	}

}
