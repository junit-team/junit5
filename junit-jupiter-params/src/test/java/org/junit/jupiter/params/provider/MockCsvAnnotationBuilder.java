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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

/**
 * @since 5.6
 */
abstract class MockCsvAnnotationBuilder<A extends Annotation, B extends MockCsvAnnotationBuilder<A, B>> {

	static CsvSource csvSource(String... lines) {
		return csvSource().lines(lines).build();
	}

	static MockCsvSourceBuilder csvSource() {
		return new MockCsvSourceBuilder();
	}

	static MockCsvFileSourceBuilder csvFileSource() {
		return new MockCsvFileSourceBuilder();
	}

	// -------------------------------------------------------------------------

	protected char delimiter = '\0';
	protected String delimiterString = "";
	protected String emptyValue = "";
	protected String[] nullValues = new String[0];

	private MockCsvAnnotationBuilder() {
	}

	protected abstract B getSelf();

	B delimiter(char delimiter) {
		this.delimiter = delimiter;
		return getSelf();
	}

	B delimiterString(String delimiterString) {
		this.delimiterString = delimiterString;
		return getSelf();
	}

	B emptyValue(String emptyValue) {
		this.emptyValue = emptyValue;
		return getSelf();
	}

	B nullValues(String... nullValues) {
		this.nullValues = nullValues;
		return getSelf();
	}

	abstract A build();

	// -------------------------------------------------------------------------

	static class MockCsvSourceBuilder extends MockCsvAnnotationBuilder<CsvSource, MockCsvSourceBuilder> {

		private String[] lines = new String[0];

		@Override
		protected MockCsvSourceBuilder getSelf() {
			return this;
		}

		MockCsvSourceBuilder lines(String... lines) {
			this.lines = lines;
			return this;
		}

		@Override
		CsvSource build() {
			CsvSource annotation = mock(CsvSource.class);

			// Common
			when(annotation.delimiter()).thenReturn(super.delimiter);
			when(annotation.delimiterString()).thenReturn(super.delimiterString);
			when(annotation.emptyValue()).thenReturn(super.emptyValue);
			when(annotation.nullValues()).thenReturn(super.nullValues);

			// @CsvSource
			when(annotation.value()).thenReturn(this.lines);

			return annotation;
		}

	}

	static class MockCsvFileSourceBuilder extends MockCsvAnnotationBuilder<CsvFileSource, MockCsvFileSourceBuilder> {

		private String[] resources = {};
		private String[] files = {};
		private String encoding = "UTF-8";
		private String lineSeparator = "\n";
		private int numLinesToSkip = 0;

		@Override
		protected MockCsvFileSourceBuilder getSelf() {
			return this;
		}

		MockCsvFileSourceBuilder resources(String... resources) {
			this.resources = resources;
			return this;
		}

		MockCsvFileSourceBuilder files(String... files) {
			this.files = files;
			return this;
		}

		MockCsvFileSourceBuilder encoding(String encoding) {
			this.encoding = encoding;
			return this;
		}

		MockCsvFileSourceBuilder lineSeparator(String lineSeparator) {
			this.lineSeparator = lineSeparator;
			return this;
		}

		MockCsvFileSourceBuilder numLinesToSkip(int numLinesToSkip) {
			this.numLinesToSkip = numLinesToSkip;
			return this;
		}

		@Override
		CsvFileSource build() {
			CsvFileSource annotation = mock(CsvFileSource.class);

			// Common
			when(annotation.delimiter()).thenReturn(super.delimiter);
			when(annotation.delimiterString()).thenReturn(super.delimiterString);
			when(annotation.emptyValue()).thenReturn(super.emptyValue);
			when(annotation.nullValues()).thenReturn(super.nullValues);

			// @CsvFileSource
			when(annotation.resources()).thenReturn(this.resources);
			when(annotation.files()).thenReturn(this.files);
			when(annotation.encoding()).thenReturn(this.encoding);
			when(annotation.lineSeparator()).thenReturn(this.lineSeparator);
			when(annotation.numLinesToSkip()).thenReturn(this.numLinesToSkip);

			return annotation;
		}

	}

}
