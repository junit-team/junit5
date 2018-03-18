package org.junit.jupiter.theories.suppliers;

import org.junit.jupiter.theories.annotations.suppliers.DoubleValues;

import java.lang.annotation.Annotation;
import java.util.Arrays;

class DoubleTheoryArgumentSupplierTests extends SupplierTestBase<DoubleTheoryArgumentSupplier, DoubleValues, Double> {

    public DoubleTheoryArgumentSupplierTests() {
        super(DoubleTheoryArgumentSupplier.class, DoubleValues.class, Double.class);
    }

    @Override
    protected AnnotationExpectedResultPair getAnnotationExpectedResultPair() {
        return new AnnotationExpectedResultPair(
                new DoubleValues() {
                    @Override
                    public double[] value() {
                        double[] testValues = {1.0, 2.0, 4.0, 8.0, 16.0};
                        return testValues;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return DoubleValues.class;
                    }
                },
                Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0)
        );
    }
}