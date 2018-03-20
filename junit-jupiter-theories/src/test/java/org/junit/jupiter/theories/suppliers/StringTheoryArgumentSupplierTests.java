
package org.junit.jupiter.theories.suppliers;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.junit.jupiter.theories.annotations.suppliers.StringValues;

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
