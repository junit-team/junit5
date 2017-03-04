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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationInitialized;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

class CsvFileArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<CsvFileSource> {

	private final BiFunction<Class<?>, String, InputStream> inputStreamProvider;

	private String resource;
	private Charset charset;
	private CsvParserSettings settings;

	CsvFileArgumentsProvider() {
		this(Class::getResourceAsStream);
	}

	CsvFileArgumentsProvider(BiFunction<Class<?>, String, InputStream> inputStreamProvider) {
		this.inputStreamProvider = inputStreamProvider;
	}

	@Override
	public void initialize(CsvFileSource annotation) {
		resource = annotation.resource();
		charset = Charset.forName(annotation.encoding());
		settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(annotation.delimiter());
		settings.getFormat().setLineSeparator(annotation.lineSeparator());
		settings.setAutoConfigurationEnabled(false);
	}

	@Override
	public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
		CsvParser csvParser = new CsvParser(settings);
		csvParser.beginParsing(openReader(context));
		return stream(spliteratorUnknownSize(new CsvParserIterator(csvParser), Spliterator.ORDERED), false) //
				.onClose(csvParser::stopParsing);
	}

	private Reader openReader(ContainerExtensionContext context) {
		Class<?> testClass = context.getTestClass().orElseThrow(
			() -> new JUnitException("Cannot load classpath resource without test class"));
		InputStream inputStream = Preconditions.notNull(inputStreamProvider.apply(testClass, resource),
			() -> "Classpath resource does not exist: " + resource);
		return new BufferedReader(new InputStreamReader(inputStream, charset));
	}

	private static class CsvParserIterator implements Iterator<Arguments> {

		private final CsvParser csvParser;

		private Object[] nextArguments;

		CsvParserIterator(CsvParser csvParser) {
			this.csvParser = csvParser;
			advance();
		}

		@Override
		public boolean hasNext() {
			return nextArguments != null;
		}

		@Override
		public Arguments next() {
			Arguments result = ObjectArrayArguments.create(nextArguments);
			advance();
			return result;
		}

		private void advance() {
			nextArguments = csvParser.parseNext();
		}
	}
}
