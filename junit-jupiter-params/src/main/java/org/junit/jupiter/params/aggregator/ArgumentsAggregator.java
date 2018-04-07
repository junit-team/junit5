/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;

/**
 * {@code ArgumentsAggregator} is an abstraction for the aggregation of a
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest}
 * method's arguments into a single object.
 *
 * <p>An {@code ArgumentsAggregator} is applied to a method parameter of a
 * {@code @ParameterizedTest} method via the {@link AggregateWith @AggregateWith}
 * annotation.
 *
 * <p>The result of the aggregation will be passed as an argument to the
 * {@code @ParameterizedTest} method for the annotated parameter.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.2
 * @see AggregateWith
 * @see ArgumentsAccessor
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@API(status = EXPERIMENTAL, since = "5.2")
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
