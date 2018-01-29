/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code Arguments} is an abstraction that provides access to an array of
 * objects to be used for invoking a {@code @ParameterizedTest} method.
 *
 * <p>A {@link java.util.stream.Stream} of such {@code Arguments} will
 * typically be provided by an {@link ArgumentsProvider}.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 */
@API(status = EXPERIMENTAL, since = "5.0")
public interface Arguments {

	/**
	 * Get the arguments used for an invocation of the
	 * {@code @ParameterizedTest} method.
	 *
	 * @return the arguments; must not be {@code null}
	 */
	Object[] get();

	/**
	 * Factory method for creating an instance of {@code Arguments} based on
	 * the supplied {@code arguments}.
	 *
	 * @param arguments the arguments to be used for an invocation of the test
	 * method; must not be {@code null}
	 * @return an instance of {@code Arguments}; never {@code null}
	 */
	static Arguments of(Object... arguments) {
		Preconditions.notNull(arguments, "argument array must not be null");
		return () -> arguments;
	}

}
