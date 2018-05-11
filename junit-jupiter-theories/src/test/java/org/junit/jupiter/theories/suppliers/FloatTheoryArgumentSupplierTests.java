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
 * Tests for {@link FloatTheoryArgumentSupplier}.
 */
class FloatTheoryArgumentSupplierTests extends SupplierTestBase<FloatTheoryArgumentSupplier, FloatValues, Float> {
	public FloatTheoryArgumentSupplierTests() {
		super(FloatTheoryArgumentSupplier.class, FloatValues.class, Float.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new FloatValues() {
			@Override
			public float[] value() {
				float[] testValues = { 1.0f, 2.0f, 4.0f, 8.0f, 16.0f };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return FloatValues.class;
			}
		}, Arrays.asList(1.0f, 2.0f, 4.0f, 8.0f, 16.0f));
	}
}
