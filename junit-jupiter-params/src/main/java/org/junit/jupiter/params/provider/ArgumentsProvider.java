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

import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An {@code ArgumentsProvider} is responsible for {@linkplain #provideArguments
 * providing} a stream of arguments to be passed to a {@code @ParameterizedTest}
 * method.
 *
 * <p>An {@code ArgumentsProvider} can be registered via the
 * {@link ArgumentsSource @ArgumentsSource} annotation.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.Arguments
 * @see org.junit.jupiter.params.support.AnnotationConsumer
 */
@API(status = STABLE, since = "5.7")
public interface ArgumentsProvider {

	/**
	 * Provide a {@link Stream} of {@link Arguments} to be passed to a
	 * {@code @ParameterizedTest} method.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return a stream of arguments; never {@code null}
	 */
	Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception;

}
