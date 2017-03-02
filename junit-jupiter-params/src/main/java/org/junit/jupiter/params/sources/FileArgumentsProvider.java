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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;
import org.junit.jupiter.params.support.ObjectArrayArguments;

class FileArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<FileSource> {

	private Path path;
	private Charset charset;
	private CsvParserSettings settings;
	private CsvParser csvParser;

	@Override
	public void initialize(FileSource annotation) throws IOException {
		path = Paths.get(annotation.path());
		charset = Charset.forName(annotation.encoding());
		settings = new CsvParserSettings();
		settings.getFormat().setDelimiter(annotation.delimiter());
		settings.getFormat().setLineSeparator(annotation.lineSeparator());
		settings.setAutoConfigurationEnabled(false);
	}

	@Override
	public Iterator<? extends Arguments> arguments(ContainerExtensionContext context) throws IOException {
		csvParser = new CsvParser(settings);
		csvParser.beginParsing(Files.newBufferedReader(path, charset));
		return new Iterator<Arguments>() {
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
	}

	@Override
	public void close() {
		if (csvParser != null) {
			csvParser.stopParsing();
		}
	}
}
