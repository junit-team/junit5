/*
 * Copyright 2015 the original author or authors.
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
 * {@code @AfterAll} is used to signal that the annotated method should be
 * executed <em>after</em> <strong>all</strong> tests in the current test
 * class or test class hierarchy.
 *
 * <p>In contrast to {@code @AfterEach} methods, {@code @AfterAll} methods
 * are only executed once for a given test class.
 *
 * <p>{@code @AfterAll} methods must not be {@code private} but may
 * optionally be {@code static} if the test class is annotated with
 * {@link TestInstance @TestInstance(PER_CLASS)}.
 *
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 * @see BeforeAll
 * @see BeforeEach
 * @see AfterEach
 * @see TestInstance
 * @see Test
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterAll {
}
