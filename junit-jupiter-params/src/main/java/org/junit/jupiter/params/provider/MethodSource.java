/*
 * Copyright 2015-2018 the original author or authors.
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
import java.util.Iterator;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * {@code @MethodSource} is an {@link ArgumentsSource} which provides access
 * to values returned from {@linkplain #value() factory methods} of the class in
 * which this annotation is declared or from static factory methods in external
 * classes referenced by <em>fully qualified method name</em>.
 *
 * <p>Each factory method must return a {@link Stream}, {@link Iterable},
 * {@link Iterator}, or array of arguments. The returned values will be provided
 * as arguments to the annotated {@link ParameterizedTest @ParameterizedTest}
 * method. If the parameterized test has a single parameter, each factory method
 * may return value instances, e.g. as {@code Stream<String>} for a single
 * {@code String} parameter, directly. If a parameterized test method declares
 * multiple parameters, factory methods must return instances of
 * {@link Arguments}, e.g. as {@code Stream<Arguments>}.
 *
 * <p>Factory methods within the test class must be must be {@code static} unless
 * the {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS PER_CLASS}
 * test instance lifecycle mode is used; whereas, factory methods in external
 * classes must always be {@code static}. In any case, factory methods must not
 * declare any parameters.
 *
 * @since 5.0
 * @see Arguments
 * @see ArgumentsSource
 * @see ParameterizedTest
 * @see org.junit.jupiter.api.TestInstance
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.0")
@ArgumentsSource(MethodArgumentsProvider.class)
public @interface MethodSource {

	/**
	 * The names of factory methods within the test class or in external classes
	 * to use as sources for arguments.
	 *
	 * <p>Factory methods in external classes must be referenced by <em>fully
	 * qualified method name</em> &mdash; for example,
	 * {@code com.example.StringsProviders#blankStrings}.
	 *
	 * <p>If no factory method names are declared, a method within the test class
	 * that has the same name as the test method will be used as the factory
	 * method by default.
	 *
	 * <p>For further information, see the {@linkplain MethodSource class-level JavaDoc}.
	 */
	String[] value() default "";

}
