package org.junit.jupiter.theories.annotations.suppliers;

import org.junit.jupiter.theories.suppliers.DoubleParameterArgumentSupplier;

import java.lang.annotation.*;


/**
 * Parameter argument supplier annotation that can be added to a theory parameter to specify the exact values that will be used for that parameter. Provides
 * {@code double} values.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ParametersSuppliedBy(DoubleParameterArgumentSupplier.class)
public @interface DoubleValues {
    /**
     * @return the value(s) to use for the annotated theory parameter
     */
    double[] value();
}
