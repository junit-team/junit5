/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
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
