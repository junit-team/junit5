/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

/**
 * {@code @DefaultCsvFileRowConverter} is a marker class used as a default in {@code CsvFileSource}
 * and to control row conversion in {@code CsvFileArgumentsProvider}.
 *
 * @see org.junit.jupiter.params.provider.CsvFileArgumentsProvider
 * @see org.junit.jupiter.params.provider.CsvFileSource
 * @since 5.0
 */
public class DefaultCsvFileRowConverter extends CsvFileRowConverter {

	private DefaultCsvFileRowConverter(Object[] fields) {
		super(fields);
	}
}
