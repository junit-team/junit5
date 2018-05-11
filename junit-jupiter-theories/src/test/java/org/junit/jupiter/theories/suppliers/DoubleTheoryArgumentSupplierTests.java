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
 * Tests for {@link DoubleTheoryArgumentSupplier}.
 */
class DoubleTheoryArgumentSupplierTests extends SupplierTestBase<DoubleTheoryArgumentSupplier, DoubleValues, Double> {

	public DoubleTheoryArgumentSupplierTests() {
		super(DoubleTheoryArgumentSupplier.class, DoubleValues.class, Double.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new DoubleValues() {
			@Override
			public double[] value() {
				double[] testValues = { 1.0, 2.0, 4.0, 8.0, 16.0 };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return DoubleValues.class;
			}
		}, Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0));
	}
}
