/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @BeforeAll} is used to signal that the annotated method should be
 * executed <em>before</em> <strong>all</strong> tests in the current test
 * class or test class hierarchy.
 *
 * <p>In contrast to {@code @BeforeEach} methods, {@code @BeforeAll} methods
 * are only executed once for a given test class.
 *
 * <p>{@code @BeforeAll} methods must not be {@code private} and must be
 * {@code static}.
 *
 * @since 5.0
 * @see AfterAll
 * @see BeforeEach
 * @see AfterEach
 * @see Test
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeforeAll {
}
