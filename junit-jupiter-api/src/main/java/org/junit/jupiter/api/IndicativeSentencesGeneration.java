/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @IndicativeSentencesGeneration} is used to declare a custom parameters
 * by {@code IndicativeSentences}, if this notation has some not declared
 * parameters, it will use the default values instead.
 *
 * @since 5.7
 * @see DisplayName
 * @see DisplayNameGenerator
 * @see DisplayNameGenerator.IndicativeSentences
 */
@DisplayNameGeneration(DisplayNameGenerator.IndicativeSentences.class)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = EXPERIMENTAL, since = "5.7")
public @interface IndicativeSentencesGeneration {

	String DEFAULT_SEPARATOR = ", ";
	Class<? extends DisplayNameGenerator> DEFAULT_GENERATOR = DisplayNameGenerator.Standard.class;

	/**
	 * Custom separator for indicative sentences generator.
	 */
	String separator() default "";

	/**
	 * Custom display name generator.
	 */
	Class<? extends DisplayNameGenerator> generator() default DisplayNameGenerator.Standard.class;
}
