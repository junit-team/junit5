package org.junit.jupiter.api.condition;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * {@code @DisabledOnOSWithEnvironmentVariable} is a container for one or more
 * {@link DisabledOnOsWithEnvironmentVariable @DisabledOnOSWithEnvironmentVariable} declarations.
 *
 * <p>Note, however, that use of the {@code @DisabledOnOSWithEnvironmentVariable} container
 * is completely optional since {@code @DisabledOnOSWithEnvironmentVariable} is a {@linkplain
 * java.lang.annotation.Repeatable repeatable} annotation.
 *
 * @see DisabledOnOsWithEnvironmentVariable
 * @see java.lang.annotation.Repeatable
 * @since 5.6
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.6")
public @interface DisabledOnOSWithEnvironmentVariables {

    /**
     * An array of one or more {@link DisabledOnOsWithEnvironmentVariable @DisabledOnOSWithEnvironmentVariable}
     * declarations.
     */
    DisabledOnOsWithEnvironmentVariable[] value();
}
