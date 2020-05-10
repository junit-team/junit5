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

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.CsvArgumentsProvider.handleCsvException;
import static org.junit.jupiter.params.provider.CsvParserFactory.createParserFor;
import static org.junit.platform.commons.util.CollectionUtils.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvFileArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvFileSource> {

	private final InputStreamProvider inputStreamProvider;

	private CsvFileSource annotation;
	private List<Source> sources;
	private Charset charset;
	private int numLinesToSkip;
	private CsvParser csvParser;

	CsvFileArgumentsProvider() {
		this(DefaultInputStreamProvider.INSTANCE);
	}

	CsvFileArgumentsProvider(InputStreamProvider inputStreamProvider) {
		this.inputStreamProvider = inputStreamProvider;
	}

	@Override
	public void accept(CsvFileSource annotation) {
		this.annotation = annotation;
		Stream<Source> resources = Arrays.stream(annotation.resources()).map(inputStreamProvider::classpathResource);
		Stream<Source> files = Arrays.stream(annotation.files()).map(inputStreamProvider::file);
		this.sources = Stream.concat(resources, files).collect(toList());
		this.charset = getCharsetFrom(annotation);
		this.numLinesToSkip = annotation.numLinesToSkip();
		this.csvParser = createParserFor(annotation);
	}

	private Charset getCharsetFrom(CsvFileSource annotation) {
		try {
			return Charset.forName(annotation.encoding());
		}
		catch (Exception ex) {
			throw new PreconditionViolationException("The charset supplied in " + annotation + " is invalid", ex);
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		// @formatter:off
		return Preconditions.notEmpty(this.sources, "Resources or files must not be empty")
				.stream()
				.map(source -> source.open(context))
				.map(this::beginParsing)
				.flatMap(this::toStream);
		// @formatter:on
	}

	private CsvParser beginParsing(InputStream inputStream) {
		try {
			this.csvParser.beginParsing(inputStream, this.charset);
		}
		catch (Throwable throwable) {
			handleCsvException(throwable, this.annotation);
		}
		return this.csvParser;
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
		private final Set<String> nullValues;
		private Object[] nextCsvRecord;

		CsvParserIterator(CsvParser csvParser, CsvFileSource annotation) {
			this.csvParser = csvParser;
			this.annotation = annotation;
			this.nullValues = toSet(annotation.nullValues());
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
			String[] parsedLine = null;
			try {
				parsedLine = this.csvParser.parseNext();
				if (parsedLine != null && !this.nullValues.isEmpty()) {
					for (int i = 0; i < parsedLine.length; i++) {
						if (this.nullValues.contains(parsedLine[i])) {
							parsedLine[i] = null;
						}
					}
				}
			}
			catch (Throwable throwable) {
				handleCsvException(throwable, this.annotation);
			}

			this.nextCsvRecord = parsedLine;
		}

	}

	private interface Source {

		InputStream open(ExtensionContext context);

	}

	interface InputStreamProvider {

		InputStream openClasspathResource(Class<?> baseClass, String path);

		InputStream openFile(String path);

		default Source classpathResource(String path) {
			return context -> openClasspathResource(context.getRequiredTestClass(), path);
		}

		default Source file(String path) {
			return context -> openFile(path);
		}

	}

	private static class DefaultInputStreamProvider implements InputStreamProvider {

		private static final DefaultInputStreamProvider INSTANCE = new DefaultInputStreamProvider();

		@Override
		public InputStream openClasspathResource(Class<?> baseClass, String path) {
			Preconditions.notBlank(path, () -> "Classpath resource [" + path + "] must not be null or blank");
			InputStream inputStream = baseClass.getResourceAsStream(path);
			return Preconditions.notNull(inputStream, () -> "Classpath resource [" + path + "] does not exist");
		}

		@Override
		public InputStream openFile(String path) {
			Preconditions.notBlank(path, () -> "File [" + path + "] must not be null or blank");
			try {
				return Files.newInputStream(Paths.get(path));
			}
			catch (IOException e) {
				throw new JUnitException("File [" + path + "] could not be read", e);
			}
		}

	}

}
