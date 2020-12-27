/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

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
@Target({ ElementType.TYPE, ElementType.METHOD })
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
