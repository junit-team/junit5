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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @DependsOn} is an annotation that is used to configure the
 * {@linkplain #value order} in which the annotated element (i.e., field or
 * method) should be evaluated or executed after given methods.
 *
 * <p>When used with the
 * {@link MethodOrderer.DependsOnAnnotation} {@link MethodOrderer}, the category applies to
 * <em>test methods</em>.
 *
 * @since 5.5
 * @see MethodOrderer.DependsOnAnnotation
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.5")
public @interface DependsOn {

	/**
	 * Name of the methods that must be executed before the annotated method.
	 *
	 * <p>Methods are ordered based on priority where methods whose names are
	 * provided in value() have higher priority than the annotated method.
	 */
	String[] value();
}
