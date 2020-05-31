/*
 * Copyright 2015-2020 the original author or authors.
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
 * {@code @CsvFileSource} is an {@link ArgumentsSource} which is used to load
 * comma-separated value (CSV) files from one or more classpath {@link #resources
 * resources} or {@link #files}.
 *
 * <p>The lines of these CSV files will be provided as arguments to the
 * annotated {@code @ParameterizedTest} method. Any line beginning with a
 * {@code #} symbol will be interpreted as a comment and will be ignored.
 *
 * @since 5.0
 * @see CsvSource
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.0")
@ArgumentsSource(CsvFileArgumentsProvider.class)
public @interface CsvFileSource {

	/**
	 * The CSV classpath resources to use as the sources of arguments; must not
	 * be empty unless {@link #files} is non-empty.
	 */
	String[] resources() default {};

	/**
	 * The CSV files to use as the sources of arguments; must not be empty
	 * unless {@link #resources} is non-empty.
	 */
	String[] files() default {};

	/**
	 * The encoding to use when reading the CSV files; must be a valid charset.
	 *
	 * <p>Defaults to {@code "UTF-8"}.
	 *
	 * @see java.nio.charset.StandardCharsets
	 */
	String encoding() default "UTF-8";

	/**
	 * The line separator to use when reading the CSV files; must consist of 1
	 * or 2 characters.
	 *
	 * <p>Defaults to {@code "\n"}.
	 */
	String lineSeparator() default "\n";

	/**
	 * The column delimiter character to use when reading the CSV files.
	 *
	 * <p>This is an alternative to {@link #delimiterString} and cannot be
	 * used in conjunction with {@link #delimiterString}.
	 *
	 * <p>Defaults implicitly to {@code ','}, if neither delimiter attribute is
	 * explicitly set.
	 */
	char delimiter() default '\0';

	/**
	 * The column delimiter string to use when reading the CSV files.
	 *
	 * <p>This is an alternative to {@link #delimiter} and cannot be used in
	 * conjunction with {@link #delimiter}.
	 *
	 * <p>Defaults implicitly to {@code ","}, if neither delimiter attribute is
	 * explicitly set.
	 *
	 * @since 5.6
	 */
	@API(status = EXPERIMENTAL, since = "5.6")
	String delimiterString() default "";

	/**
	 * The number of lines to skip when reading the CSV files.
	 *
	 * <p>Typically used to skip header lines.
	 *
	 * <p>Defaults to {@code 0}.
	 *
	 * @since 5.1
	 */
	@API(status = EXPERIMENTAL, since = "5.1")
	int numLinesToSkip() default 0;

	/**
	 * The empty value to use when reading the CSV files.
	 *
	 * <p>This value replaces quoted empty strings read from the input.
	 *
	 * <p>Defaults to {@code ""}.
	 *
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	String emptyValue() default "";

	/**
	 * A list of strings that should be interpreted as {@code null} references.
	 *
	 * <p>For example, you may wish for certain values such as {@code "N/A"} or
	 * {@code "NIL"} to be converted to {@code null} references.
	 *
	 * <p>Please note that <em>unquoted</em> empty values will always be converted
	 * to {@code null} references regardless of the value of this {@code nullValues}
	 * attribute; whereas, a <em>quoted</em> empty string will be treated as an
	 * {@link #emptyValue}.
	 *
	 * <p>Defaults to {@code {}}.
	 *
	 * @since 5.6
	 */
	@API(status = EXPERIMENTAL, since = "5.6")
	String[] nullValues() default {};

}
