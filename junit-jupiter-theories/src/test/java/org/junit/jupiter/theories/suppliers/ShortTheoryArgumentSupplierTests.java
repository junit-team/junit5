
package org.junit.jupiter.theories.suppliers;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.junit.jupiter.theories.annotations.suppliers.ShortValues;

/**
 * Tests for {@link ShortTheoryArgumentSupplier}.
 */
class ShortTheoryArgumentSupplierTests extends SupplierTestBase<ShortTheoryArgumentSupplier, ShortValues, Short> {
	public ShortTheoryArgumentSupplierTests() {
		super(ShortTheoryArgumentSupplier.class, ShortValues.class, Short.class);
	}

	@Override
	protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
		return new AnnotationExpectedResultPair(new ShortValues() {
			@Override
			public short[] value() {
				short[] testValues = { (short) 1, (short) 2, (short) 4, (short) 8, (short) 16 };
				return testValues;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return ShortValues.class;
			}
		}, Arrays.asList((short) 1, (short) 2, (short) 4, (short) 8, (short) 16));
	}
}
