/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

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
 * <p>{@code @ParameterizedTest} methods must specify at least one
 * {@link org.junit.jupiter.params.provider.ArgumentsProvider} via the
 * {@link org.junit.jupiter.params.provider.ArgumentsSource @ArgumentsSource}
 * or a corresponding composed annotation. The provider is responsible for
 * providing a {@link java.util.stream.Stream} of
 * {@link org.junit.jupiter.params.provider.Arguments} that will be used to
 * invoke the {@code @ParameterizedTest} method. The method may have additional
 * parameters to be resolved by other
 * {@link org.junit.jupiter.api.extension.ParameterResolver ParameterResolvers}
 * at the end of the method's parameter list.
 *
 * <p>Method parameters may use
 * {@link org.junit.jupiter.params.converter.ConvertWith @ConvertWith}
 * or a corresponding composed annotation to specify an explicit
 * {@link org.junit.jupiter.params.converter.ArgumentConverter}.
 *
 * <p>{@code @ParameterizedTest} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @ParameterizedTest}.
 *
 * <p>{@code @ParameterizedTest} methods must not be {@code private} or {@code static}.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.CsvFileSource
 * @see org.junit.jupiter.params.provider.CsvSource
 * @see org.junit.jupiter.params.provider.EnumSource
 * @see org.junit.jupiter.params.provider.MethodSource
 * @see org.junit.jupiter.params.provider.ValueSource
 * @see org.junit.jupiter.params.converter.ConvertWith
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.0")
@TestTemplate
@ExtendWith(ParameterizedTestExtension.class)
public @interface ParameterizedTest {

	/**
	 * The name pattern to be used for invocations of the parameterized test;
	 * never blank or consisting solely of whitespace.
	 *
	 * <p>You may use the following placeholders:
	 * <ul>
	 * <li><code>{index}</code>: the current invocation index (1-based)</li>
	 * <li><code>{arguments}</code>: the complete, comma-separated arguments list</li>
	 * <li><code>{0}</code>, <code>{1}</code>, etc.: an individual argument</li>
	 * </ul>
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting of values.
	 *
	 * @see java.text.MessageFormat
	 */
	String name() default "[{index}] {arguments}";

}
