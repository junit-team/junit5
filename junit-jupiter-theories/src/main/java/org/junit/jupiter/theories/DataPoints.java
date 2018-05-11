/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * Annotation that indicates that an element should be treated as a group of
 * data points for use with theories. May be used on:
 * <ul>
 * <li>Static fields containing groups of data points</li>
 * <li>Static methods returning groups of data points: Methods may be called
 * multiple times, so they must always return the same values</li>
 * <li>Non-static fields containing groups of data points: This requires that
 * the class be annotated with
 * {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}</li>
 * <li>Non-static methods: See the limitations for static methods and
 * non-static fields above.</li>
 * </ul>
 *
 * <p>Supported data point group types:
 * <ul>
 * <li>{@link Iterable}</li>
 * <li>Arrays</li>
 * <li>{@link java.util.Iterator} (Only supported for datapoint methods)</li>
 * <li>{@link java.util.stream.Stream} (Only supported for datapoint methods)</li>
 * </ul>
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@API(status = EXPERIMENTAL, since = "5.2")
public @interface DataPoints {
	/**
	 * @return the qualifier(s) for these data points. Can be empty.
	 *
	 * @see Qualifiers for additional information on how qualifiers work
	 */
	String[] qualifiers() default {};
}
