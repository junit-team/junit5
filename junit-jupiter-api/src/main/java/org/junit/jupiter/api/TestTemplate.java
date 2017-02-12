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
 * {@code @TestTemplate} is used to signal that the annotated method is a
 * <em>test template</em> method.
 *
 * <p>In contrast to {@link Test @Test} methods, a test template is not itself
 * a test case but rather a template for test cases. As such, it is designed to
 * be invoked multiple times depending on the number of {@linkplain
 * org.junit.jupiter.api.extension.TestTemplateInvocationContext invocation
 * contexts} returned by the registered {@linkplain
 * org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
 * providers}. Must be used together with at least one provider. Otherwise,
 * execution will fail.
 *
 * <p>Each invocation of a test template method, behaves like the execution of
 * a regular {@link Test @Test} method, i.e. it supports the same lifecycle
 * callbacks and extensions.
 *
 * <p>{@code @TestTemplate} methods must not be {@code private} or {@code static}
 * and must return {@code void}.
 *
 * <p>{@code @TestTemplate} methods may optionally declare parameters to be
 * resolved by {@link org.junit.jupiter.api.extension.ParameterResolver
 * ParameterResolvers}.
 *
 * @since 5.0
 * @see Test
 * @see org.junit.jupiter.api.extension.TestTemplateInvocationContext
 * @see org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@Testable
public @interface TestTemplate {
}
