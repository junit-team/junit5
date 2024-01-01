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

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code Arguments} is an abstraction that provides access to an array of
 * objects to be used for invoking a {@code @ParameterizedTest} method.
 *
 * <p>A {@link java.util.stream.Stream} of such {@code Arguments} will
 * typically be provided by an {@link ArgumentsProvider}.
 *
 * @apiNote <p>This interface is specifically designed as a simple holder of
 * arguments of a parameterized test. Therefore, if you end up
 * {@linkplain java.util.stream.Stream#map(java.util.function.Function) transforming}
 * or
 * {@linkplain java.util.stream.Stream#filter(java.util.function.Predicate) filtering}
 * the arguments, you should consider using one of the following in intermediate
 * steps:
 *
 * <ul>
 *   <li>The standard collections</li>
 *   <li>Tuples from third-party libraries, e.g.,
 *   <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/tuple/package-summary.html">Commons Lang</a>,
 *   or <a href="https://www.javatuples.org">javatuples</a></li>
 *   <li>Your own data class</li>
 * </ul>
 *
 * <p>Alternatively, you can use an
 * {@link org.junit.jupiter.params.converter.ArgumentConverter ArgumentConverter}
 * to convert some of the arguments from one type to another.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see org.junit.jupiter.params.converter.ArgumentConverter
 */
@API(status = STABLE, since = "5.7")
public interface Arguments {

	/**
	 * Get the arguments used for an invocation of the
	 * {@code @ParameterizedTest} method.
	 *
	 * @apiNote If you need a type-safe way to access some or all of the arguments,
	 * please read the {@linkplain Arguments class-level API note}.
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
	 * @see #arguments(Object...)
	 */
	static Arguments of(Object... arguments) {
		Preconditions.notNull(arguments, "argument array must not be null");
		return () -> arguments;
	}

	/**
	 * Factory method for creating an instance of {@code Arguments} based on
	 * the supplied {@code arguments}.
	 *
	 * <p>This method is an <em>alias</em> for {@link Arguments#of} and is
	 * intended to be used when statically imported &mdash; for example, via:
	 * {@code import static org.junit.jupiter.params.provider.Arguments.arguments;}
	 *
	 * @param arguments the arguments to be used for an invocation of the test
	 * method; must not be {@code null}
	 * @return an instance of {@code Arguments}; never {@code null}
	 * @since 5.3
	 */
	static Arguments arguments(Object... arguments) {
		return of(arguments);
	}

}
