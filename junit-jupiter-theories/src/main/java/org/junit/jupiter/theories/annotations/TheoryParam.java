package org.junit.jupiter.theories.annotations;

import java.lang.annotation.*;


/**
 * Annotation that indicates that a parameter should be treated as a theory parameter, meaning that it will be populated with values from data points.
 *
 * @see Theory for full usage instructions
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface TheoryParam {
    String[] qualifiers() default {};

}
