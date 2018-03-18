package org.junit.jupiter.theories.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that an element should be treated as a collection of data points for use with theories. May be used on:
 * <ul>
 * <li>Static fields containing collections</li>
 * <li>Static methods returning collections: Methods may be called multiple times, so they should always return the same values</li>
 * <li>Non-static fields containing collections: This requires that the class be annotated with {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPoints {
    /**
     * @return the qualifier(s) for these data points. Can be empty.
     *
     * @see Qualifiers for additional information on how qualifiers work
     */
    String[] qualifiers() default {};
}
