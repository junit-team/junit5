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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.gen5.commons.meta.API;

/**
 * {@code @TestFactory} is used to signal that the annotated method is a <em>test factory</em> method.
 *
 * <p>In contrast to {@code @Test} it is not ifself a test case but creates test cases.
 * Such a {@code @TestFactory} method must return a Stream or Collection of {@code DynamicTest} instances.
 * These {@code DynamicTest}s will then be executed lazily
 * enabling dynamic and even non-deterministic generation of test cases.
 *
 * <p>{@code @TestFactory} methods must not be {@code private} or {@code static}.
 *
 * <p>{@code @TestFactory} methods may optionally declare parameters to be
 * resolved by {@link org.junit.gen5.api.extension.MethodParameterResolver
 * MethodParameterResolvers}.
 *
 * @since 5.0
 * @see Test
 * @see DynamicTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
public @interface TestFactory {
}
