/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.sources;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;
import org.junit.jupiter.params.support.ObjectArrayArguments;
import org.junit.platform.commons.JUnitException;

class FileArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<FileSource> {

	private final BiFunction<Path, Charset, Reader> readerProvider;

	private Path path;
	private Charset charset;
	private CsvParserSettings settings;

	public FileArgumentsProvider() {
		this((path, charset) -> {
			try {
				return Files.newBufferedReader(path, charset);
			}
			catch (IOException e) {
				throw new JUnitException("Error opening CSV file", e);
			}
		});
	}

	FileArgumentsProvider(BiFunction<Path, Charset, Reader> readerProvider) {
		this.readerProvider = readerProvider;
	}

	@Override
	public void initialize(FileSource annotation) {
		path = Paths.get(annotation.path());
		charset = Charset.forName(annotation.encoding());
		settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(annotation.delimiter());
		settings.getFormat().setLineSeparator(annotation.lineSeparator());
		settings.setAutoConfigurationEnabled(false);
	}

	@Override
	public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
		CsvParser csvParser = new CsvParser(settings);
		csvParser.beginParsing(readerProvider.apply(path, charset));
		Iterator<Arguments> iterator = new Iterator<Arguments>() {
			@Override
			public boolean hasNext() {
				return !csvParser.getContext().isStopped();
			}

			@Override
			public Arguments next() {
				Object[] arguments = csvParser.parseNext();
				return ObjectArrayArguments.create(arguments);
			}
		};
		return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false) //
				.onClose(csvParser::stopParsing);
	}
}
