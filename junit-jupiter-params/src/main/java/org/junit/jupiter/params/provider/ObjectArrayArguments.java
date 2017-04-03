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
 * Concrete implementation of the {@code Arguments} API that provides access to
 * an array of objects to be used for invoking a {@code @ParameterizedTest} method.
 *
 * <p>A {@link java.util.stream.Stream} of such {@code Arguments} will
 * typically be accessed via {@linkplain ArgumentsProvider providers}.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 */
@API(Experimental)
public class ObjectArrayArguments implements Arguments {

	private final Object[] arguments;

	/**
	 * Factory method for creating an {@code ObjectArrayArguments} based on the
	 * supplied arguments
	 */
	public static ObjectArrayArguments arguments(Object... arguments) {
		return new ObjectArrayArguments(arguments);
	}

	private ObjectArrayArguments(Object... arguments) {
		this.arguments = arguments;
	}

	@Override
	public Object[] get() {
		return this.arguments;
	}

}
