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
 * Tests for {@link LongTheoryArgumentSupplier}.
 */
class LongTheoryArgumentSupplierTests extends SupplierTestBase<LongTheoryArgumentSupplier, LongValues, Long> {
	public LongTheoryArgumentSupplierTests() {
		super(LongTheoryArgumentSupplier.class, LongValues.class, Long.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new LongValues() {
			@Override
			public long[] value() {
				long[] testValues = { 1L, 2L, 4L, 8L, 16L };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return LongValues.class;
			}
		}, Arrays.asList(1L, 2L, 4L, 8L, 16L));
	}
}
