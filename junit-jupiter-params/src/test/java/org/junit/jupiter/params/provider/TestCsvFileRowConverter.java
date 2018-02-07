/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import org.junit.jupiter.params.converter.CsvFileRowConverter;

public class TestCsvFileRowConverter extends CsvFileRowConverter {

	public TestCsvFileRowConverter(Object[] fields) {
		super(fields);
	}

	public String getFirst() {
		return String.valueOf(fields[0]);
	}
}
