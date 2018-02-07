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
 * {@code @CsvFileRowConverter} is an abstract parent for CSV file row converters.
 *
 * <p> It ensures that converters are constructed with an {@code Object} array of fields.
 *
 * @see org.junit.jupiter.params.provider.CsvFileArgumentsProvider
 * @see org.junit.jupiter.params.provider.CsvFileSource
 * @since 5.0
 */
public class CsvFileRowConverter {

	protected Object[] fields;

	public CsvFileRowConverter(Object[] fields) {
		this.fields = fields;
	}

}
