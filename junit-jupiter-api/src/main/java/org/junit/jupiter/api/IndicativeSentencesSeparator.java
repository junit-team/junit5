/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @IndicativeSentencesSeparator} is used to declare a custom separator
 * in the IndicativeSentencesGenerator, if this notation was not declared
 * IndicativeSentencesGenerator will use ", " as separator by default.
 *
 * @see DisplayNameGenerator
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@API(status = EXPERIMENTAL, since = "5.4")
public @interface IndicativeSentencesSeparator {

	/**
	 * Custom separator for indicative sentences generator.
	 *
	 * @return custom separator for indicative sentences
	 */
	String value();
}
