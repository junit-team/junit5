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
 * Tests for {@link IntTheoryArgumentSupplier}.
 */
class IntTheoryArgumentSupplierTests extends SupplierTestBase<IntTheoryArgumentSupplier, IntValues, Integer> {
	public IntTheoryArgumentSupplierTests() {
		super(IntTheoryArgumentSupplier.class, IntValues.class, Integer.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new IntValues() {
			@Override
			public int[] value() {
				int[] testValues = { 1, 2, 4, 8, 16 };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return IntValues.class;
			}
		}, Arrays.asList(1, 2, 4, 8, 16));
	}
}
