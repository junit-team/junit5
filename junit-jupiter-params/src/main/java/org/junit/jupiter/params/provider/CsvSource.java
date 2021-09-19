/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @CsvSource} is an {@link ArgumentsSource} which reads comma-separated
 * values (CSV) from one or more CSV lines supplied via the {@link #value}
 * attribute or {@link #textBlock} attribute.
 *
 * <p>The column delimiter (defaults to comma) can be customized with either
 * {@link #delimiter()} or {@link #delimiterString()}.
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
@API(status = STABLE, since = "5.7")
@ArgumentsSource(CsvArgumentsProvider.class)
public @interface CsvSource {

	/**
	 * The CSV lines to use as the source of arguments; must not be empty.
	 *
	 * <p>Each value corresponds to a line in a CSV file and will be split using
	 * the specified {@link #delimiter} or {@link #delimiterString}. Any line
	 * beginning with a {@code #} symbol will be interpreted as a comment and will
	 * be ignored.
	 *
	 * <p>Defaults to an empty array. You therefore must supply CSV content
	 * via this attribute or the {@link #textBlock} attribute.
	 *
	 * <p>If <em>text block</em> syntax is supported by your programming language,
	 * you may find it more convenient to declare your CSV content via the
	 * {@link #textBlock} attribute.
	 *
	 * <h4>Example</h4>
	 * <pre class="code">
	 * {@literal @}ParameterizedTest
	 * {@literal @}CsvSource({
	 *     "apple,         1",
	 *     "banana,        2",
	 *     "'lemon, lime', 0xF1",
	 *     "strawberry,    700_000",
	 * })
	 * void test(String fruit, int rank) {
	 *     // ...
	 * }</pre>
	 *
	 * @see #textBlock
	 */
	String[] value() default {};

	/**
	 * The CSV lines to use as the source of arguments, supplied as a single
	 * <em>text block</em>; must not be empty.
	 *
	 * <p>Each line in the text block corresponds to a line in a CSV file and will
	 * be split using the specified {@link #delimiter} or {@link #delimiterString}.
	 * Any line beginning with a {@code #} symbol will be interpreted as a comment
	 * and will be ignored.
	 *
	 * <p>Defaults to an empty string. You therefore must supply CSV content
	 * via this attribute or the {@link #value} attribute.
	 *
	 * <p>Text block syntax is supported by various languages on the JVM
	 * including Java SE 15 or higher. If text blocks are not supported, you
	 * should declare your CSV content via the {@link #value} attribute.
	 *
	 * <h4>Example</h4>
	 * <pre class="code">
	 * {@literal @}ParameterizedTest
	 * {@literal @}CsvSource(textBlock = """
	 *     apple,         1
	 *     banana,        2
	 *     'lemon, lime', 0xF1
	 *     strawberry,    700_000
	 * """)
	 * void test(String fruit, int rank) {
	 *     // ...
	 * }</pre>
	 *
	 * @since 5.8.1
	 * @see #value
	 */
	@API(status = EXPERIMENTAL, since = "5.8.1")
	String textBlock() default "";

	/**
	 * The column delimiter character to use when reading the {@linkplain #value lines}.
	 *
	 * <p>This is an alternative to {@link #delimiterString} and cannot be
	 * used in conjunction with {@link #delimiterString}.
	 *
	 * <p>Defaults implicitly to {@code ','}, if neither delimiter attribute is
	 * explicitly set.
	 */
	char delimiter() default '\0';

	/**
	 * The column delimiter string to use when reading the {@linkplain #value lines}.
	 *
	 * <p>This is an alternative to {@link #delimiter} and cannot be used in
	 * conjunction with {@link #delimiter}.
	 *
	 * <p>Defaults implicitly to {@code ","}, if neither delimiter attribute is
	 * explicitly set.
	 *
	 * @since 5.6
	 */
	String delimiterString() default "";

	/**
	 * The empty value to use when reading the {@linkplain #value lines}.
	 *
	 * <p>This value replaces quoted empty strings read from the input.
	 *
	 * <p>Defaults to {@code ""}.
	 *
	 * @since 5.5
	 */
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
	String[] nullValues() default {};

	/**
	 * The maximum characters of per CSV column allowed.
	 *
	 * <p>Must be a positive number.
	 *
	 * <p>Defaults to {@code 4096}.
	 *
	 * @since 5.7
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	int maxCharsPerColumn() default 4096;

	/**
	 * Identifies whether leading and trailing whitespace characters of
	 * unquoted CSV columns should be ignored.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * @since 5.8
	 */
	@API(status = EXPERIMENTAL, since = "5.8")
	boolean ignoreLeadingAndTrailingWhitespace() default true;

}
