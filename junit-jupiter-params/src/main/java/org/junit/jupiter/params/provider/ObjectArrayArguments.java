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

import org.junit.platform.commons.meta.API;

/**
 * Concrete implementation of the {@code Arguments} abstraction.
 *
 * It provides access to an array of objects to be used for invoking a
 * {@code @ParameterizedTest} method.
 *
 * A {@link java.util.stream.Stream} of such {@code Arguments} will
 * typically be accessed via {@linkplain ArgumentsProvider providers}.
 *
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @since 5.0
 */
@API(Experimental)
public class ObjectArrayArguments implements Arguments {

	private final Object[] arguments;

	public static ObjectArrayArguments create(Object... arguments) {
		return new ObjectArrayArguments(arguments);
	}

	private ObjectArrayArguments(Object... arguments) {
		this.arguments = arguments;
	}

	@Override
	public Object[] get() {
		return arguments;
	}
}
