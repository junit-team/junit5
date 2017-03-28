/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @ValueSource} is a special {@link ArgumentsSource} which provides
 * access to an array of literal values of certain primitive types.
 * These primitive types include String, int, long, and double.
 *
 * These values are then provided as arguments to {@code @ParameterizedTest}
 * methods using a {@code @ValueArgumentsProvider}.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ValueArgumentsProvider
 * @see org.junit.jupiter.params.ParameterizedTest
 * @since 5.0
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@ArgumentsSource(ValueArgumentsProvider.class)
public @interface ValueSource {

	String[] strings() default {};

	int[] ints() default {};

	long[] longs() default {};

	double[] doubles() default {};

}
