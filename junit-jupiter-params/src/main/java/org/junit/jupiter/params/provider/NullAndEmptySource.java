/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @NullAndEmptySource} is a <em>composed annotation</em> that combines
 * the functionality of {@link NullSource @NullSource} and
 * {@link EmptySource @EmptySource}.
 *
 * <p>Annotating a {@code @ParameterizedTest} method with
 * {@code @NullAndEmptySource} is equivalent to annotating the method with
 * {@code @NullSource} and {@code @EmptySource}.
 *
 * @since 5.4
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see NullSource
 * @see EmptySource
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.7")
@NullSource
@EmptySource
public @interface NullAndEmptySource {
}
