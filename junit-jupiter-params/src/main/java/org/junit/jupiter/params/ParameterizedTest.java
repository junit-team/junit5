/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.meta.API;

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
 * <p>Method parameters may use the
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
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.CsvFileSource
 * @see org.junit.jupiter.params.provider.CsvSource
 * @see org.junit.jupiter.params.provider.EnumSource
 * @see org.junit.jupiter.params.provider.MethodSource
 * @see org.junit.jupiter.params.provider.ValueSource
 * @see org.junit.jupiter.params.converter.ConvertWith
 * @since 5.0
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@TestTemplate
@ExtendWith(ParameterizedTestExtension.class)
public @interface ParameterizedTest {

	/**
	 * TODO Document name().
	 *
	 * @return a custom display name; never blank or consisting solely of
	 * whitespace
	 */
	String name() default "[{index}] {arguments}";

}
