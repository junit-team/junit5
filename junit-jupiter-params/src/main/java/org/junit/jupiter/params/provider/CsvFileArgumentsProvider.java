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

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvFileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvFileSource> {

	private final BiFunction<Class<?>, String, InputStream> inputStreamProvider;

	private String[] resources;
	private Charset charset;
	private CsvParserSettings settings;

	CsvFileArgumentsProvider() {
		this(Class::getResourceAsStream);
	}

	CsvFileArgumentsProvider(BiFunction<Class<?>, String, InputStream> inputStreamProvider) {
		this.inputStreamProvider = inputStreamProvider;
	}

	@Override
	public void accept(CsvFileSource annotation) {
		resources = annotation.resources();
		charset = Charset.forName(annotation.encoding());
		settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(annotation.delimiter());
		settings.getFormat().setLineSeparator(annotation.lineSeparator());
		settings.setAutoConfigurationEnabled(false);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ContainerExtensionContext context) {
		// @formatter:off
		return Arrays.stream(resources)
				.map(resource -> openInputStream(context, resource))
				.map(this::createCsvParser)
				.flatMap(this::toStream);
		// @formatter:on
	}

	private InputStream openInputStream(ContainerExtensionContext context, String resource) {
		Class<?> testClass = context.getTestClass().orElseThrow(
			() -> new JUnitException("Cannot load classpath resource without test class"));
		return Preconditions.notNull(inputStreamProvider.apply(testClass, resource),
			() -> "Classpath resource does not exist: " + resource);
	}

	private CsvParser createCsvParser(InputStream inputStream) {
		CsvParser csvParser = new CsvParser(settings);
		csvParser.beginParsing(inputStream, charset);
		return csvParser;
	}

	private Stream<Arguments> toStream(CsvParser csvParser) {
		return stream(spliteratorUnknownSize(new CsvParserIterator(csvParser), Spliterator.ORDERED), false) //
				.onClose(csvParser::stopParsing);
	}

	private static class CsvParserIterator implements Iterator<Arguments> {

		private final CsvParser csvParser;

		private Object[] nextCsvRecord;

		CsvParserIterator(CsvParser csvParser) {
			this.csvParser = csvParser;
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
			this.nextCsvRecord = csvParser.parseNext();
		}
	}

}
