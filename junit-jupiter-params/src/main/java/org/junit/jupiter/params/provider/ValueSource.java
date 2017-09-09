/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @ValueSource} is an {@link ArgumentsSource} which provides
 * access to an array of literal values of primitive types.
 *
 * <p>Supported primitive types include {@link #strings}, {@link #ints},
 * {@link #longs}, and {@link #doubles}. You must not specify more than one
 * annotation attribute.
 *
 * <p>The supplied literal values will be provided as arguments to the
 * annotated {@code @ParameterizedTest} method.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.0")
@ArgumentsSource(ValueArgumentsProvider.class)
public @interface ValueSource {

	/**
	 * The {@link String} values to use as sources of arguments; must not be
	 * empty.
	 */
	String[] strings() default {};

	/**
	 * The {@code int} values to use as sources of arguments; must not be empty.
	 */
	int[] ints() default {};

	/**
	 * The {@code long} values to use as sources of arguments; must not be empty.
	 */
	long[] longs() default {};

	/**
	 * The {@code double} values to use as sources of arguments; must not be
	 * empty.
	 */
	double[] doubles() default {};

}
