package org.junit.jupiter.params.provider;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * {@code @AllBooleanCombinationsSource} is an {@link ArgumentsSource} for
 * constants of an {@link Boolean}.
 *
 * <p>The boolean combinations will be provided as arguments to the annotated
 * {@code @ParameterizedTest} method.
 *
 * <p>The count of boolean values can be specified explicitly using the
 * {@link #value} attribute. Otherwise, 1 is used.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.8.2")
@ArgumentsSource(AllBooleanCombinationsArgumentsProvider.class)
public @interface AllBooleanCombinationsSource {
    int value() default 1;
}
