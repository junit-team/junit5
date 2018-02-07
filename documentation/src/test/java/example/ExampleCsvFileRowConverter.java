/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import org.junit.jupiter.params.converter.CsvFileRowConverter;

// tag::ExampleCsvFileRowConverter_example[]
public class ExampleCsvFileRowConverter extends CsvFileRowConverter {
	public ExampleCsvFileRowConverter(Object[] fields) {
		super(fields);
	}
	public String getCountry() {
		return String.valueOf(fields[0]);
	}
	public int getReference() {
		return Integer.valueOf(String.valueOf(fields[1]));
	}
}
// end::ExampleCsvFileRowConverter_example[]
