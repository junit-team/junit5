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

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.platform.commons.annotation.Testable;

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
 * <p>Each invocation of a test template method behaves like the execution of
 * a regular {@link Test @Test} method with full support for the same lifecycle
 * callbacks and extensions.
 *
 * <p>{@code @TestTemplate} methods must not be {@code private} or {@code static}
 * and must return {@code void}.
 *
 * <p>{@code @TestTemplate} methods may optionally declare parameters to be
 * resolved by {@link org.junit.jupiter.api.extension.ParameterResolver
 * ParameterResolvers}.
 *
 * <p>{@code @TestTemplate} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @TestTemplate}.
 *
 * @since 5.0
 * @see Test
 * @see org.junit.jupiter.api.extension.TestTemplateInvocationContext
 * @see org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.0")
@Testable
public @interface TestTemplate {
}
