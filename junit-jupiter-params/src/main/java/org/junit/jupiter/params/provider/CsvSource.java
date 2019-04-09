/*
 * Copyright 2015-2019 the original author or authors.
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
 * {@code @CsvSource} is an {@link ArgumentsSource} which reads
 * comma-separated values (CSV) from one or more supplied
 * {@linkplain #value CSV lines}.
 *
 * <p>The supplied values will be provided as arguments to the
 * annotated {@code @ParameterizedTest} method.
 *
 * @since 5.0
 * @see CsvFileSource
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.0")
@ArgumentsSource(CsvArgumentsProvider.class)
public @interface CsvSource {

	/**
	 * The CSV lines to use as source of arguments; must not be empty.
	 *
	 * <p>Each value corresponds to a line in a CSV file and will be split using
	 * the specified {@link #delimiter delimiter}.
	 */
	String[] value();

	/**
	 * The column delimiter to use when reading the {@linkplain #value lines}.
	 *
	 * <p>Defaults to {@code ','}.
	 */
	char delimiter() default ',';

	/**
	 * The empty value to use when reading the {@linkplain #value lines}.
	 *
	 * <p>This value replaces quoted empty strings read from the input.
	 *
	 * <p>Defaults to {@code ""}.
	 *
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	String emptyValue() default "";

}
