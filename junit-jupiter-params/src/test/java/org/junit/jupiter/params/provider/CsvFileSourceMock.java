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

class CsvFileSourceMock {

	private String[] resources;
	private String charset = "UTF-8";
	private String lineSeparator = "\n";
	private char delimiter = '\0';
	private String emptyValue = "";
	private int numLinesToSkip = 0;

	private CsvFileSourceMock() {
	}

	static CsvFileSourceMock builder() {
		return new CsvFileSourceMock();
	}

	CsvFileSourceMock resources(String... resources) {
		this.resources = resources;
		return this;
	}

	CsvFileSourceMock charset(String charset) {
		this.charset = charset;
		return this;
	}

	CsvFileSourceMock lineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
		return this;
	}

	CsvFileSourceMock delimiter(char delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	CsvFileSourceMock emptyValue(String emptyValue) {
		this.emptyValue = emptyValue;
		return this;
	}

	CsvFileSourceMock numLinesToSkip(int numLinesToSkip) {
		this.numLinesToSkip = numLinesToSkip;
		return this;
	}

	CsvFileSource build() {
		CsvFileSource annotation = mock(CsvFileSource.class);
		when(annotation.resources()).thenReturn(resources);
		when(annotation.encoding()).thenReturn(charset);
		when(annotation.lineSeparator()).thenReturn(lineSeparator);
		when(annotation.delimiter()).thenReturn(delimiter);
		when(annotation.emptyValue()).thenReturn(emptyValue);
		when(annotation.numLinesToSkip()).thenReturn(numLinesToSkip);
		return annotation;
	}
}
