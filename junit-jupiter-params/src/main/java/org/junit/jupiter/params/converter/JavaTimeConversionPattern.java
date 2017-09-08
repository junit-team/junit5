/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import static org.junit.platform.commons.meta.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.meta.API;

/**
 * {@code @JavaTimeConversionPattern} is an annotation that allows a date/time
 * conversion pattern to be specified on a method parameter of a
 * {@link ParameterizedTest @ParameterizedTest} method.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see java.time.format.DateTimeFormatterBuilder#appendPattern(String)
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL)
@ConvertWith(JavaTimeArgumentConverter.class)
public @interface JavaTimeConversionPattern {

	/**
	 * The date/time conversion pattern.
	 *
	 * @see java.time.format.DateTimeFormatterBuilder#appendPattern(String)
	 */
	String value();

}
