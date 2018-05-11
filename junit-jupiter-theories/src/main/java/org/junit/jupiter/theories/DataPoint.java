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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * Annotation that indicates that an element should be treated as a data point
 * for use with theories. May be used on:
 * <ul>
 * <li>Static fields</li>
 * <li>Static methods: Methods may be called multiple times, so they should
 * always return the same values</li>
 * <li>Non-static fields: This requires that the class be annotated with
 * {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}</li>
 * <li>Non-static methods: See the limitations for static methods and
 * non-static fields above.</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target({ METHOD, FIELD })
@API(status = EXPERIMENTAL, since = "5.2")
public @interface DataPoint {
	/**
	 * @return the qualifier(s) for this data point. Can be empty.
	 *
	 * @see Qualifiers for additional information on how qualifiers work
	 */
	String[] qualifiers() default {};
}
