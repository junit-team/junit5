package org.junit.jupiter.theories.annotations.suppliers;

import org.junit.jupiter.theories.suppliers.ShortParameterArgumentSupplier;

import java.lang.annotation.*;

/**
 * Parameter argument supplier annotation that can be added to a theory parameter to specify the exact values that will be used for that parameter. Provides
 * {@code short} values.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ParametersSuppliedBy(ShortParameterArgumentSupplier.class)
public @interface ShortValues {
    /**
     * @return the value(s) to use for the annotated theory parameter
     */
    short[] value();
}
