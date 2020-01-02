/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @EmptySource} is an {@link ArgumentsSource} which provides a single
 * <em>empty</em> argument to the annotated {@code @ParameterizedTest} method.
 *
 * <h3>Supported Parameter Types</h3>
 *
 * <p>This argument source will only provide an empty argument for the following
 * method parameter types. Subtypes of the supported types are not supported.
 *
 * <ul>
 * <li>{@link java.lang.String}</li>
 * <li>{@link java.util.List}</li>
 * <li>{@link java.util.Set}</li>
 * <li>{@link java.util.Map}</li>
 * <li>primitive arrays &mdash; for example {@code int[]}, {@code char[][]}, etc.</li>
 * <li>object arrays &mdash; for example {@code String[]}, {@code Integer[][]}, etc.</li>
 * </ul>
 *
 * @since 5.4
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see NullSource
 * @see NullAndEmptySource
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.4")
@ArgumentsSource(EmptyArgumentsProvider.class)
public @interface EmptySource {
}
