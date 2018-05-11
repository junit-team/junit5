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
 * Tests for {@link ByteTheoryArgumentSupplier}.
 */
class ByteTheoryArgumentSupplierTests extends SupplierTestBase<ByteTheoryArgumentSupplier, ByteValues, Byte> {
	public ByteTheoryArgumentSupplierTests() {
		super(ByteTheoryArgumentSupplier.class, ByteValues.class, Byte.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new ByteValues() {
			@Override
			public byte[] value() {
				byte[] testValues = { (byte) 1, (byte) 2, (byte) 4, (byte) 8, (byte) 16 };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return ByteValues.class;
			}
		}, Arrays.asList((byte) 1, (byte) 2, (byte) 4, (byte) 8, (byte) 16));
	}
}
