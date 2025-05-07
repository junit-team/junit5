/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.params.ParameterizedTest;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * {@code @BooleansSource} is a composed annotation that provides a convenient
 * way to specify {@code true} and {@code false} as arguments for
 * a {@link ParameterizedTest @ParameterizedTest} method.
 *
 * <p>It serves as a shorthand for {@code @ValueSource(booleans = {true, false})},
 * commonly used for testing behavior with feature flags or boolean conditions.
 *
 * <h2>Composition</h2>
 *
 * <p>This annotation is composed of {@link ValueSource} with predefined boolean values.
 * It is designed to be a more readable and maintainable alternative to explicitly
 * specifying boolean values in {@code @ValueSource}.
 *
 * @since 5.13
 * @see ValueSource
 * @see ArgumentsSource
 * @see ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ValueSource(booleans = { true, false })
@API(status = EXPERIMENTAL, since = "5.13")
public @interface BooleansSource {
}
