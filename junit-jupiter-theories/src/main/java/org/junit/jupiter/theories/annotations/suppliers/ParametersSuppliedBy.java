package org.junit.jupiter.theories.annotations.suppliers;

import org.junit.jupiter.theories.suppliers.ParameterArgumentSupplier;

import java.lang.annotation.*;

/**
 * Meta-annotation that indicates that an annotation is a parameter argument supplier annotation. The class specified in {@code value} will be used to convert
 * the annotation into data point details.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ParametersSuppliedBy {
    /**
     * @return the type that converts the meta-annotated annotation into data point details
     */
    Class<? extends ParameterArgumentSupplier> value();
}
