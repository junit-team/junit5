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

import java.util.Optional;
import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code Arguments} is an abstraction that provides access to an array of
 * objects to be used for invoking a {@code @ParameterizedTest} method.
 * An optional description may be provided and included in the
 * {@linkplain org.junit.jupiter.params.ParameterizedTest#name() name of the parameterized test}.
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
	 * Returns the number of arguments. Non-negative.
	 *
	 * @since 5.2
	 */
	default int size() {
		return get().length;
	}

	/**
	 * Returns a description of these arguments. If present, not blank.
	 *
	 * @since 5.2
	 * @see #describedAs(String)
	 */
	default Optional<String> getDescription() {
		return Optional.empty();
	}

	/**
	 * Creates a new instance of Arguments with the given description. It will have the same
	 * set of arguments and a non-blank description.
	 *
	 * @param testCaseDescription a description of this set of arguments. Must not be blank.
	 * @return a new instance of Arguments with the given description
	 * @throws org.junit.platform.commons.util.PreconditionViolationException if the description
	 *     is blank
	 * @since 5.2
	 */
	default Arguments describedAs(String testCaseDescription) {
		Preconditions.notBlank(testCaseDescription,
				() -> "Test case description must not be blank: '" + testCaseDescription + "'! " +
						"Simply do not set it: it’s optional.");

		Object[] arguments = get();
		Optional<String> trimmedDesc = Optional.of(testCaseDescription.trim());

		return new Arguments() {
			@Override
			public Object[] get() {
				return arguments;
			}

			@Override
			public Optional<String> getDescription() {
				return trimmedDesc;
			}
		};
	}

	/**
	 * Factory method for creating an instance of {@code Arguments} based on
	 * the supplied {@code arguments}.
	 *
	 * <p>The produced instance of {@code Arguments} will have empty description.
	 * Use {@link #describedAs(String)} to set a description of these arguments.
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
