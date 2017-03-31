/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.platform.commons.meta.API;

/**
 * Implementations of {@code ArgumentsProvider} are responsible for providing a
 * {@link Stream} of {@code Arguments}.
 *
 * <p>Such a stream will then be used for invoking a {@code @ParameterizedTest} method.
 *
 * <p>{@code ArgumentsProvider}s are registered via {@code @ArgumentsSource} annotations.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.Arguments
 */
@API(Experimental)
public interface ArgumentsProvider {

	Stream<? extends Arguments> arguments(ContainerExtensionContext context) throws Exception;

}
