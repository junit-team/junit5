/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Stable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.meta.API;

/**
 * {@code @Test} is used to signal that the annotated method is a
 * <em>test</em> method.
 *
 * <p>{@code @Test} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @Test}.
 *
 * <p>{@code @Test} methods must not be {@code private} or {@code static}.
 *
 * <p>{@code @Test} methods may optionally declare parameters to be
 * resolved by {@link org.junit.jupiter.api.extension.ParameterResolver
 * ParameterResolvers}.
 *
 * @since 5.0
 * @see TestInfo
 * @see DisplayName
 * @see BeforeEach
 * @see AfterEach
 * @see BeforeAll
 * @see AfterAll
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Stable)
@Testable
public @interface Test {
}
