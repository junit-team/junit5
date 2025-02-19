/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * @since 5.13
 * @see Parameter
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.13")
@ContainerTemplate
@ExtendWith(ParameterizedContainerExtension.class)
@SuppressWarnings("exports")
public @interface ParameterizedContainer {

	/**
	 * The display name to be used for individual invocations of the
	 * parameterized container; never blank or consisting solely of whitespace.
	 *
	 * <p>Defaults to <code>{@value ParameterizedInvocationNameFormatter#DEFAULT_DISPLAY_NAME}</code>.
	 *
	 * <p>If the default display name flag
	 * (<code>{@value ParameterizedInvocationNameFormatter#DEFAULT_DISPLAY_NAME}</code>)
	 * is not overridden, JUnit will:
	 * <ul>
	 * <li>Look up the {@value ParameterizedInvocationNameFormatter#DISPLAY_NAME_PATTERN_KEY}
	 * <em>configuration parameter</em> and use it if available. The configuration
	 * parameter can be supplied via the {@code Launcher} API, build tools (e.g.,
	 * Gradle and Maven), a JVM system property, or the JUnit Platform configuration
	 * file (i.e., a file named {@code junit-platform.properties} in the root of
	 * the class path). Consult the User Guide for further information.</li>
	 * <li>Otherwise, <code>{@value ParameterizedInvocationConstants#DEFAULT_DISPLAY_NAME}</code> will be used.</li>
	 * </ul>
	 *
	 * <h4>Supported placeholders</h4>
	 * <ul>
	 * <li><code>{@value ParameterizedInvocationConstants#DISPLAY_NAME_PLACEHOLDER}</code></li>
	 * <li><code>{@value ParameterizedInvocationConstants#INDEX_PLACEHOLDER}</code></li>
	 * <li><code>{@value ParameterizedInvocationConstants#ARGUMENT_SET_NAME_PLACEHOLDER}</code></li>
	 * <li><code>{@value ParameterizedInvocationConstants#ARGUMENTS_PLACEHOLDER}</code></li>
	 * <li><code>{@value ParameterizedInvocationConstants#ARGUMENTS_WITH_NAMES_PLACEHOLDER}</code></li>
	 * <li><code>{@value ParameterizedInvocationConstants#ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER}</code></li>
	 * <li><code>"{0}"</code>, <code>"{1}"</code>, etc.: an individual argument (0-based)</li>
	 * </ul>
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting (for example, {@code {0,number,#.###}}). Please
	 * note that the original arguments are passed when formatting, regardless
	 * of any implicit or explicit argument conversions.
	 *
	 * <p>Note that
	 * <code>{@value ParameterizedInvocationNameFormatter#DEFAULT_DISPLAY_NAME}</code> is
	 * a flag rather than a placeholder.
	 *
	 * @see java.text.MessageFormat
	 */
	String name() default ParameterizedInvocationNameFormatter.DEFAULT_DISPLAY_NAME;

	/**
	 * Configure whether all arguments of the parameterized container that
	 * implement {@link AutoCloseable} will be closed after their corresponding
	 * invocation.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * <p><strong>WARNING</strong>: if an argument that implements
	 * {@code AutoCloseable} is reused for multiple invocations of the same
	 * parameterized container, you must set {@code autoCloseArguments} to
	 * {@code false} to ensure that the argument is not closed between
	 * invocations.
	 *
	 * @see java.lang.AutoCloseable
	 */
	boolean autoCloseArguments() default true;

	/**
	 * Configure whether zero invocations are allowed for this
	 * parameterized container.
	 *
	 * <p>Set this attribute to {@code true} if the absence of invocations is
	 * expected in some cases and should not cause a test failure.
	 *
	 * <p>Defaults to {@code false}.
	 */
	boolean allowZeroInvocations() default false;

	/**
	 * Configure how the number of arguments provided by an
	 * {@link ArgumentsSource} are validated.
	 *
	 * <p>Defaults to {@link ArgumentCountValidationMode#DEFAULT}.
	 *
	 * <p>When an {@link ArgumentsSource} provides more arguments than declared
	 * by the parameterized container class constructor or {@link Parameter}
	 * annotated fields, there might be a bug in the method or the
	 * {@link ArgumentsSource}. By default, the additional arguments are
	 * ignored. {@code argumentCountValidation} allows you to control how
	 * additional arguments are handled. The default can be configured via the
	 * {@value ArgumentCountValidator#ARGUMENT_COUNT_VALIDATION_KEY}
	 * configuration parameter (see the User Guide for details on configuration
	 * parameters).
	 *
	 * @see ArgumentCountValidationMode
	 */
	ArgumentCountValidationMode argumentCountValidation() default ArgumentCountValidationMode.DEFAULT;

}
