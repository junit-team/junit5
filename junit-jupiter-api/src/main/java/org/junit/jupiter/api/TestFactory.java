/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.meta.API;

/**
 * {@code @TestFactory} is used to signal that the annotated method is a
 * <em>test factory</em> method.
 *
 * <p>In contrast to {@link Test @Test} methods, a test factory is not itself
 * a test case but rather a factory for test cases.
 *
 * <p>{@code @TestFactory} methods must not be {@code private} or {@code static}
 * and must return a {@code Stream}, {@code Collection}, {@code Iterable}, or
 * {@code Iterator} of {@link DynamicTest} instances. These {@code DynamicTests}
 * will then be executed lazily, enabling dynamic and even non-deterministic
 * generation of test cases.
 *
 * any Stream returned by a {@code @TestFactory} will be properly closed,
 * making it safe to use a resource for example {@code Files.lines()}.
 *
 * <p>{@code @TestFactory} methods may optionally declare parameters to be
 * resolved by {@link org.junit.jupiter.api.extension.ParameterResolver
 * ParameterResolvers}.
 *
 * @since 5.0
 * @see Test
 * @see DynamicTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@Testable
public @interface TestFactory {
}
