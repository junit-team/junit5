package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.IntValues;

import java.lang.annotation.Annotation;
import java.util.Arrays;

class IntTheoryArgumentSupplierTests extends SupplierTestBase<IntTheoryArgumentSupplier, IntValues, Integer> {
    public IntTheoryArgumentSupplierTests() {
        super(IntTheoryArgumentSupplier.class, IntValues.class, Integer.class);
    }

    @Override
    protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
        return new AnnotationExpectedResultPair(
                new IntValues() {
                    @Override
                    public int[] value() {
                        int[] testValues = {1, 2, 4, 8, 16};
                        return testValues;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return IntValues.class;
                    }
                },
                Arrays.asList(1, 2, 4, 8, 16)
        );
    }
}