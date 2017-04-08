/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @CsvFileSource} is a special {@link ArgumentsSource} which is used to
 * declare comma-separated values (CSV) files from one or more classpath resources.
 *
 * <p>The lines of these CSV files are then provided as arguments to
 * {@code @ParameterizedTest} methods using a {@code CsvFileArgumentsProvider}.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.CsvFileArgumentsProvider
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@ArgumentsSource(CsvFileArgumentsProvider.class)
public @interface CsvFileSource {

	String[] resources();

	String encoding() default "UTF-8";

	String lineSeparator() default "\n";

	char delimiter() default ',';

}
