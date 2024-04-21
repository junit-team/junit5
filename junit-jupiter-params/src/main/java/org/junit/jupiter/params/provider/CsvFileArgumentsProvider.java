/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.junit.jupiter.params.provider.CsvArgumentsProvider.getHeaders;
import static org.junit.jupiter.params.provider.CsvArgumentsProvider.handleCsvException;
import static org.junit.jupiter.params.provider.CsvArgumentsProvider.processCsvRecord;
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
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvFileArgumentsProvider extends AnnotationBasedArgumentsProvider<CsvFileSource> {

	private final InputStreamProvider inputStreamProvider;

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
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, CsvFileSource csvFileSource) {
		this.charset = getCharsetFrom(csvFileSource);
		this.numLinesToSkip = csvFileSource.numLinesToSkip();
		this.csvParser = createParserFor(csvFileSource);

		Stream<Source> resources = Arrays.stream(csvFileSource.resources()).map(inputStreamProvider::classpathResource);
		Stream<Source> files = Arrays.stream(csvFileSource.files()).map(inputStreamProvider::file);
		List<Source> sources = Stream.concat(resources, files).collect(toList());

		// @formatter:off
		return Preconditions.notEmpty(sources, "Resources or files must not be empty")
				.stream()
				.map(source -> source.open(context))
				.map(inputStream -> beginParsing(inputStream, csvFileSource))
				.flatMap(parser -> toStream(parser, csvFileSource));
		// @formatter:on
	}

	private Charset getCharsetFrom(CsvFileSource csvFileSource) {
		try {
			return Charset.forName(csvFileSource.encoding());
		}
		catch (Exception ex) {
			throw new PreconditionViolationException("The charset supplied in " + csvFileSource + " is invalid", ex);
		}
	}

	private CsvParser beginParsing(InputStream inputStream, CsvFileSource csvFileSource) {
		try {
			this.csvParser.beginParsing(inputStream, this.charset);
		}
		catch (Throwable throwable) {
			handleCsvException(throwable, csvFileSource);
		}
		return this.csvParser;
	}

	private Stream<Arguments> toStream(CsvParser csvParser, CsvFileSource csvFileSource) {
		CsvParserIterator iterator = new CsvParserIterator(csvParser, csvFileSource);
		return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false) //
				.skip(this.numLinesToSkip) //
				.onClose(() -> {
					try {
						csvParser.stopParsing();
					}
					catch (Throwable throwable) {
						handleCsvException(throwable, csvFileSource);
					}
				});
	}

	private static class CsvParserIterator implements Iterator<Arguments> {

		private final CsvParser csvParser;
		private final CsvFileSource csvFileSource;
		private final boolean useHeadersInDisplayName;
		private final Set<String> nullValues;
		private Arguments nextArguments;
		private String[] headers;

		CsvParserIterator(CsvParser csvParser, CsvFileSource csvFileSource) {
			this.csvParser = csvParser;
			this.csvFileSource = csvFileSource;
			this.useHeadersInDisplayName = csvFileSource.useHeadersInDisplayName();
			this.nullValues = toSet(csvFileSource.nullValues());
			advance();
		}

		@Override
		public boolean hasNext() {
			return this.nextArguments != null;
		}

		@Override
		public Arguments next() {
			Arguments result = this.nextArguments;
			advance();
			return result;
		}

		private void advance() {
			try {
				String[] csvRecord = this.csvParser.parseNext();
				if (csvRecord != null) {
					// Lazily retrieve headers if necessary.
					if (this.useHeadersInDisplayName && this.headers == null) {
						this.headers = getHeaders(this.csvParser);
					}
					this.nextArguments = processCsvRecord(csvRecord, this.nullValues, this.useHeadersInDisplayName,
						this.headers);
				}
				else {
					this.nextArguments = null;
				}
			}
			catch (Throwable throwable) {
				handleCsvException(throwable, this.csvFileSource);
			}
		}

	}

	@FunctionalInterface
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
