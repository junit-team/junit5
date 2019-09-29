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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

/**
 * @since 5.6
 */
class MockCsvAnnotationBuilder<A extends Annotation> {

	static CsvSource csvSource(String... lines) {
		return csvSource().lines(lines).build();
	}

	static MockCsvAnnotationBuilder<CsvSource> csvSource() {
		return new MockCsvAnnotationBuilder<>() {
			// anonymous inner class to capture generic type information
		};
	}

	static MockCsvAnnotationBuilder<CsvFileSource> csvFileSource() {
		return new MockCsvAnnotationBuilder<>() {
			// anonymous inner class to capture generic type information
		};
	}

	private final Class<? extends Annotation> annotationType;

	// Common
	private char delimiter = '\0';
	private String delimiterString = "";
	private String emptyValue = "";
	private String[] nullValues = new String[0];

	// @CsvSource
	private String[] lines = new String[0];

	// @CsvFileSource
	private String[] resources = { "test.csv" };
	private String encoding = "UTF-8";
	private String lineSeparator = "\n";
	private int numLinesToSkip = 0;

	@SuppressWarnings("unchecked")
	private MockCsvAnnotationBuilder() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		this.annotationType = (Class<? extends Annotation>) parameterizedType.getActualTypeArguments()[0];
	}

	// -- Common ---------------------------------------------------------------

	MockCsvAnnotationBuilder<A> delimiter(char delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	MockCsvAnnotationBuilder<A> delimiterString(String delimiterString) {
		this.delimiterString = delimiterString;
		return this;
	}

	MockCsvAnnotationBuilder<A> emptyValue(String emptyValue) {
		this.emptyValue = emptyValue;
		return this;
	}

	MockCsvAnnotationBuilder<A> nullValues(String... nullValues) {
		this.nullValues = nullValues;
		return this;
	}

	// -- @CsvSource -----------------------------------------------------------

	MockCsvAnnotationBuilder<A> lines(String... lines) {
		onlyForCsvSource();
		this.lines = lines;
		return this;
	}

	// -- @CsvFileSource -------------------------------------------------------

	/**
	 * Defaults to "test.csv".
	 */
	MockCsvAnnotationBuilder<A> resources(String... resources) {
		onlyForCsvFileSource();
		this.resources = resources;
		return this;
	}

	MockCsvAnnotationBuilder<A> encoding(String encoding) {
		onlyForCsvFileSource();
		this.encoding = encoding;
		return this;
	}

	MockCsvAnnotationBuilder<A> lineSeparator(String lineSeparator) {
		onlyForCsvFileSource();
		this.lineSeparator = lineSeparator;
		return this;
	}

	MockCsvAnnotationBuilder<A> numLinesToSkip(int numLinesToSkip) {
		onlyForCsvFileSource();
		this.numLinesToSkip = numLinesToSkip;
		return this;
	}

	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	A build() {
		if (this.annotationType == CsvSource.class) {
			return (A) buildCsvSource();
		}
		if (this.annotationType == CsvFileSource.class) {
			return (A) buildCsvFileSource();
		}
		// Appease the compiler
		throw new IllegalStateException("Unsupported annotation type: " + this.annotationType.getName());
	}

	private CsvSource buildCsvSource() {
		CsvSource annotation = mock(CsvSource.class);

		// Common
		when(annotation.delimiter()).thenReturn(this.delimiter);
		when(annotation.delimiterString()).thenReturn(this.delimiterString);
		when(annotation.emptyValue()).thenReturn(this.emptyValue);
		when(annotation.nullValues()).thenReturn(this.nullValues);

		// @CsvSource
		when(annotation.value()).thenReturn(this.lines);

		return annotation;
	}

	private CsvFileSource buildCsvFileSource() {
		CsvFileSource annotation = mock(CsvFileSource.class);

		// Common
		when(annotation.delimiter()).thenReturn(this.delimiter);
		when(annotation.delimiterString()).thenReturn(this.delimiterString);
		when(annotation.emptyValue()).thenReturn(this.emptyValue);
		when(annotation.nullValues()).thenReturn(this.nullValues);

		// @CsvFileSource
		when(annotation.resources()).thenReturn(this.resources);
		when(annotation.encoding()).thenReturn(this.encoding);
		when(annotation.lineSeparator()).thenReturn(this.lineSeparator);
		when(annotation.numLinesToSkip()).thenReturn(this.numLinesToSkip);

		return annotation;
	}

	private void onlyForCsvSource() {
		if (!(this.annotationType == CsvSource.class)) {
			throw new UnsupportedOperationException("Only supported for @CsvSource");
		}
	}

	private void onlyForCsvFileSource() {
		if (!(this.annotationType == CsvFileSource.class)) {
			throw new UnsupportedOperationException("Only supported for @CsvFileSource");
		}
	}

}
