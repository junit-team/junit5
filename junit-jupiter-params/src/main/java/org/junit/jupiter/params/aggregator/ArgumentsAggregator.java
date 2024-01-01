/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;

/**
 * {@code ArgumentsAggregator} is an abstraction for the aggregation of arguments
 * provided by an {@link org.junit.jupiter.params.provider.ArgumentsProvider
 * ArgumentsProvider} for a single invocation of a
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest} method
 * into a single object.
 *
 * <p>An {@code ArgumentsAggregator} is applied to a method parameter of a
 * {@code @ParameterizedTest} method via the {@link AggregateWith @AggregateWith}
 * annotation.
 *
 * <p>The result of the aggregation will be passed as an argument to the
 * {@code @ParameterizedTest} method for the annotated parameter.
 *
 * <p>A common use case is the aggregation of multiple columns from a single line
 * in a CSV file into a domain object such as a {@code Person}, {@code Address},
 * {@code Order}, etc.
 *
 * <p>Implementations must provide a no-args constructor and should not make any
 * assumptions regarding when they are instantiated or how often they are called.
 * Since instances may potentially be cached and called from different threads,
 * they should be thread-safe and designed to be used as singletons.
 *
 * @since 5.2
 * @see AggregateWith
 * @see ArgumentsAccessor
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@API(status = STABLE, since = "5.7")
public interface ArgumentsAggregator {

	/**
	 * Aggregate the arguments contained in the supplied {@code accessor} into a
	 * single object.
	 *
	 * @param accessor an {@link ArgumentsAccessor} containing the arguments to be
	 * aggregated; never {@code null}
	 * @param context the parameter context where the aggregated result is to be
	 * supplied; never {@code null}
	 * @return the aggregated result; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ArgumentsAggregationException if an error occurs during the
	 * aggregation
	 */
	Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
			throws ArgumentsAggregationException;

}
