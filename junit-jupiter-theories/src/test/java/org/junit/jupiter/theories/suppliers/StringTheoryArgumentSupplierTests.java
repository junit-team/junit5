/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.suppliers;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * Tests for {@link StringTheoryArgumentSupplier}.
 */
class StringTheoryArgumentSupplierTests extends SupplierTestBase<StringTheoryArgumentSupplier, StringValues, String> {
	public StringTheoryArgumentSupplierTests() {
		super(StringTheoryArgumentSupplier.class, StringValues.class, String.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new StringValues() {
			@Override
			public String[] value() {
				String[] testValues = { "one", "two", "four", "eight", "sixteen" };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return StringValues.class;
			}
		}, Arrays.asList("one", "two", "four", "eight", "sixteen"));
	}
}
