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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.univocity.parsers.common.DefaultConversionProcessor;
import com.univocity.parsers.common.processor.ObjectRowListProcessor;
import com.univocity.parsers.conversions.Conversions;
import com.univocity.parsers.csv.CsvParser;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

	private static final String LINE_SEPARATOR = "\n";

	private CsvSource annotation;
	private CsvParser csvParser;

	@Override
	public void accept(CsvSource annotation) {
		this.annotation = annotation;
		this.csvParser = CsvParserFactory.createParserFor(annotation);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

		AtomicLong index = new AtomicLong(0);
		DefaultConversionProcessor processor = getConversionProcessor();

		// @formatter:off
        return Arrays.stream(this.annotation.value())
                .map(line -> {
                    String[] parsedLine = null;
                    try {
                        parsedLine = this.csvParser.parseLine(line + LINE_SEPARATOR);
                        if (parsedLine != null) {
                            parsedLine = Arrays.copyOf(processor.applyConversions(parsedLine, null), parsedLine.length, String[].class);
                        }
                    } catch (Throwable throwable) {
                        handleCsvException(throwable, this.annotation);
                    }
                    Preconditions.notNull(parsedLine,
                                          () -> "Line at index " + index.get() + " contains invalid CSV: \"" + line + "\"");
                    return parsedLine;
                })
                .peek(values -> index.incrementAndGet())
                .map(Arguments::of);
        // @formatter:on
	}

	private DefaultConversionProcessor getConversionProcessor() {
		ObjectRowListProcessor processor = new ObjectRowListProcessor();
		if (this.annotation.nullSymbols().length > 0) {
			processor.convertAll(Conversions.toNull(this.annotation.nullSymbols()));
		}

		return processor;
	}

	static void handleCsvException(Throwable throwable, Annotation annotation) {
		BlacklistedExceptions.rethrowIfBlacklisted(throwable);
		if (throwable instanceof PreconditionViolationException) {
			throw (PreconditionViolationException) throwable;
		}
		throw new CsvParsingException("Failed to parse CSV input configured via " + annotation, throwable);
	}

}
