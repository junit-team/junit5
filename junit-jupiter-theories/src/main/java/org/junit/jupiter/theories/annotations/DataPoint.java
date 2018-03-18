package org.junit.jupiter.theories.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that indicates that an element should be treated as a data point for use with theories. May be used on:
 * <ul>
 * <li>Static fields</li>
 * <li>Static methods: Methods may be called multiple times, so they should always return the same values</li>
 * <li>Non-static fields: This requires that the class be annotated with {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface DataPoint {
    /**
     * @return the qualifier(s) for this data point. Can be empty.
     *
     * @see Qualifiers for additional information on how qualifiers work
     */
    public String[] qualifiers() default {};
}
