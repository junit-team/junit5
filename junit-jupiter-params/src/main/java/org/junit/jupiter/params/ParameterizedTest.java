/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @ParameterizedTest} is used to signal that the annotated method is a
 * <em>parameterized test</em> method.
 *
 * <p>Such methods must not be {@code private} or {@code static}.
 *
 * <h2>Arguments Providers and Sources</h2>
 *
 * <p>{@code @ParameterizedTest} methods must specify at least one
 * {@link org.junit.jupiter.params.provider.ArgumentsProvider ArgumentsProvider}
 * via {@link org.junit.jupiter.params.provider.ArgumentsSource @ArgumentsSource}
 * or a corresponding composed annotation (e.g., {@code @ValueSource},
 * {@code @CsvSource}, etc.). The provider is responsible for providing a
 * {@link java.util.stream.Stream Stream} of
 * {@link org.junit.jupiter.params.provider.Arguments Arguments} that will be
 * used to invoke the parameterized test method.
 *
 * <h2>Formal Parameter List</h2>
 *
 * <p>A {@code @ParameterizedTest} method may declare additional parameters at
 * the end of the method's parameter list to be resolved by other
 * {@link org.junit.jupiter.api.extension.ParameterResolver ParameterResolvers}
 * (e.g., {@code TestInfo}, {@code TestReporter}, etc). Specifically, a
 * parameterized test method must declare formal parameters according to the
 * following rules.
 *
 * <ol>
 * <li>Zero or more <em>indexed arguments</em> must be declared first.</li>
 * <li>Zero or more <em>aggregators</em> must be declared next.</li>
 * <li>Zero or more arguments supplied by other {@code ParameterResolver}
 * implementations must be declared last.</li>
 * </ol>
 *
 * <p>In this context, an <em>indexed argument</em> is an argument for a given
 * index in the {@code Arguments} provided by an {@code ArgumentsProvider} that
 * is passed as an argument to the parameterized method at the same index in the
 * method's formal parameter list. An <em>aggregator</em> is any parameter of type
 * {@link org.junit.jupiter.params.aggregator.ArgumentsAccessor ArgumentsAccessor}
 * or any parameter annotated with
 * {@link org.junit.jupiter.params.aggregator.AggregateWith @AggregateWith}.
 *
 * <h2>Argument Conversion</h2>
 *
 * <p>Method parameters may be annotated with
 * {@link org.junit.jupiter.params.converter.ConvertWith @ConvertWith}
 * or a corresponding composed annotation to specify an <em>explicit</em>
 * {@link org.junit.jupiter.params.converter.ArgumentConverter ArgumentConverter}.
 * Otherwise, JUnit Jupiter will attempt to perform an <em>implicit</em>
 * conversion to the target type automatically (see the User Guide for further
 * details).
 *
 * <h2>Composed Annotations</h2>
 *
 * <p>{@code @ParameterizedTest} may also be used as a meta-annotation in order
 * to create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @ParameterizedTest}.
 *
 * <h2>Test Execution Order</h2>
 *
 * <p>By default, test methods will be ordered using an algorithm that is
 * deterministic but intentionally nonobvious. This ensures that subsequent runs
 * of a test suite execute test methods in the same order, thereby allowing for
 * repeatable builds. In this context, a <em>test method</em> is any instance
 * method that is directly annotated or meta-annotated with {@code @Test},
 * {@code @RepeatedTest}, {@code @ParameterizedTest}, {@code @TestFactory}, or
 * {@code @TestTemplate}.
 *
 * <p>Although true <em>unit tests</em> typically should not rely on the order
 * in which they are executed, there are times when it is necessary to enforce
 * a specific test method execution order &mdash; for example, when writing
 * <em>integration tests</em> or <em>functional tests</em> where the sequence of
 * the tests is important, especially in conjunction with
 * {@link org.junit.jupiter.api.TestInstance @TestInstance(Lifecycle.PER_CLASS)}.
 *
 * <p>To control the order in which test methods are executed, annotate your
 * test class or test interface with
 * {@link org.junit.jupiter.api.TestMethodOrder @TestMethodOrder} and specify
 * the desired {@link org.junit.jupiter.api.MethodOrderer MethodOrderer}
 * implementation.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.Arguments
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.CsvFileSource
 * @see org.junit.jupiter.params.provider.CsvSource
 * @see org.junit.jupiter.params.provider.EnumSource
 * @see org.junit.jupiter.params.provider.MethodSource
 * @see org.junit.jupiter.params.provider.ValueSource
 * @see org.junit.jupiter.params.aggregator.ArgumentsAccessor
 * @see org.junit.jupiter.params.aggregator.AggregateWith
 * @see org.junit.jupiter.params.converter.ArgumentConverter
 * @see org.junit.jupiter.params.converter.ConvertWith
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.7")
@TestTemplate
@ExtendWith(ParameterizedTestExtension.class)
@SuppressWarnings("exports")
public @interface ParameterizedTest {

	/**
	 * Placeholder for the {@linkplain org.junit.jupiter.api.TestInfo#getDisplayName
	 * display name} of a {@code @ParameterizedTest} method: <code>{displayName}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	/**
	 * Placeholder for the current invocation index of a {@code @ParameterizedTest}
	 * method (1-based): <code>{index}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String INDEX_PLACEHOLDER = "{index}";

	/**
	 * Placeholder for the complete, comma-separated arguments list of the
	 * current invocation of a {@code @ParameterizedTest} method:
	 * <code>{arguments}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String ARGUMENTS_PLACEHOLDER = "{arguments}";

	/**
	 * Placeholder for the complete, comma-separated named arguments list
	 * of the current invocation of a {@code @ParameterizedTest} method:
	 * <code>{argumentsWithNames}</code>
	 *
	 * <p>Argument names will be retrieved via the {@link java.lang.reflect.Parameter#getName()}
	 * API if the byte code contains parameter names &mdash; for example, if
	 * the code was compiled with the {@code -parameters} command line argument
	 * for {@code javac}.
	 *
	 * @since 5.6
	 * @see #name
	 */
	String ARGUMENTS_WITH_NAMES_PLACEHOLDER = "{argumentsWithNames}";

	/**
	 * Default display name pattern for the current invocation of a
	 * {@code @ParameterizedTest} method: {@value}
	 *
	 * <p>Note that the default pattern does <em>not</em> include the
	 * {@linkplain #DISPLAY_NAME_PLACEHOLDER display name} of the
	 * {@code @ParameterizedTest} method.
	 *
	 * @since 5.3
	 * @see #name
	 * @see #DISPLAY_NAME_PLACEHOLDER
	 * @see #INDEX_PLACEHOLDER
	 * @see #ARGUMENTS_WITH_NAMES_PLACEHOLDER
	 */
	String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_WITH_NAMES_PLACEHOLDER;

	/**
	 * The display name to be used for individual invocations of the
	 * parameterized test; never blank or consisting solely of whitespace.
	 *
	 * <p>Defaults to <code>{default_display_name}</code>.
	 *
	 * <p>If the default display name flag (<code>{default_display_name}</code>)
	 * is not overridden, JUnit will:
	 * <ul>
	 * <li>Look up the {@value ParameterizedTestExtension#DISPLAY_NAME_PATTERN_KEY}
	 * <em>configuration parameter</em> and use it if available. The configuration
	 * parameter can be supplied via the {@code Launcher} API, build tools (e.g.,
	 * Gradle and Maven), a JVM system property, or the JUnit Platform configuration
	 * file (i.e., a file named {@code junit-platform.properties} in the root of
	 * the class path). Consult the User Guide for further information.</li>
	 * <li>Otherwise, the value of the {@link #DEFAULT_DISPLAY_NAME} constant will
	 * be used.</li>
	 * </ul>
	 *
	 * <h4>Supported placeholders</h4>
	 * <ul>
	 * <li>{@link #DISPLAY_NAME_PLACEHOLDER}</li>
	 * <li>{@link #INDEX_PLACEHOLDER}</li>
	 * <li>{@link #ARGUMENTS_PLACEHOLDER}</li>
	 * <li>{@link #ARGUMENTS_WITH_NAMES_PLACEHOLDER}</li>
	 * <li><code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)</li>
	 * </ul>
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting. Please note that the original arguments are
	 * passed when formatting, regardless of any implicit or explicit argument
	 * conversions.
	 *
	 * <p>Note that <code>{default_display_name}</code> is a flag rather than a
	 * placeholder.
	 *
	 * @see java.text.MessageFormat
	 */
	String name() default "{default_display_name}";

	/**
	 * Configure whether all arguments of the parameterized test that implement {@link AutoCloseable}
	 * will be closed after {@link org.junit.jupiter.api.AfterEach @AfterEach} methods
	 * and {@link org.junit.jupiter.api.extension.AfterEachCallback AfterEachCallback}
	 * extensions have been called for the current parameterized test invocation.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * <p><strong>WARNING</strong>: if an argument that implements {@code AutoCloseable}
	 * is reused for multiple invocations of the same parameterized test method,
	 * you must set {@code autoCloseArguments} to {@code false} to ensure that
	 * the argument is not closed between invocations.
	 *
	 * @since 5.8
	 * @see java.lang.AutoCloseable
	 */
	@API(status = STABLE, since = "5.10")
	boolean autoCloseArguments() default true;

}
