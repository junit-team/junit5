/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @EnabledIfEnvironmentVariable} is used to signal that the annotated test
 * class or test method is only <em>enabled</em> if the value of the specified
 * {@linkplain #named environment variable} matches the specified
 * {@linkplain #matches regular expression}.
 *
 * <p>When declared at the class level, the result will apply to all test methods
 * within that class as well.
 *
 * <p>If the specified environment variable is undefined, the presence of this
 * annotation will have no effect on whether or not the class or method
 * is enabled.
 *
 * @since 5.1
 * @see DisabledIfEnvironmentVariable
 * @see org.junit.jupiter.api.Disabled
 * @see org.junit.jupiter.api.EnabledIf
 * @see org.junit.jupiter.api.DisabledIf
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(EnabledIfEnvironmentVariableCondition.class)
@API(status = STABLE, since = "5.1")
public @interface EnabledIfEnvironmentVariable {

	/**
	 * The name of the environment variable to retrieve.
	 *
	 * @return the environment variable name; never <em>blank</em>
	 * @see System#getenv(String)
	 */
	String named();

	/**
	 * A regular expression that will be used to match against the retrieved
	 * value of the {@link #named} environment variable.
	 *
	 * @return the regular expression; never <em>blank</em>
	 * @see String#matches(String)
	 * @see java.util.regex.Pattern
	 */
	String matches();

}
