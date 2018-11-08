/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

/**
 * {@code @Order} is a method-level annotation that is used to configure the
 * {@linkplain #value order} in which the annotated method should be executed
 * relative to other methods of the same category.
 *
 * <p>When used with the {@link OrderAnnotation} {@link MethodOrderer}, the
 * category applies to <em>test methods</em>.
 *
 * <p>If {@code @Order} is not explicitly declared on a method, the default
 * order value assigned to the method is {@link Integer#MAX_VALUE}.
 *
 * @since 5.4
 * @see MethodOrderer.OrderAnnotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.4")
public @interface Order {

	/**
	 * The order value for the annotated method.
	 *
	 * <p>Methods are ordered based on priority where a lower value has greater
	 * priority than a higher value. For example, {@link Integer#MAX_VALUE} has
	 * the lowest priority.
	 */
	int value();

}
