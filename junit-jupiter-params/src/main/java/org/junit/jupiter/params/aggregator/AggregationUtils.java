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

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apiguardian.api.API;

/**
 * Collection of utilities for working with aggregating argument consumers
 * in parameterized tests (i.e., parameters of type {@link ArgumentsAccessor}
 * or annotated with {@link AggregateWith @AggregateWith}).
 *
 * @since 5.2
 */
@API(status = INTERNAL, since = "5.2")
public class AggregationUtils {

	///CLOVER:OFF
	private AggregationUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Determine if the supplied {@link Method} has a <em>potentially</em>
	 * valid signature (i.e., formal parameter declarations) with regard to
	 * aggregators.
	 *
	 * <p>This method takes a best-effort approach at enforcing the following
	 * policy for parameterized test methods that accept aggregators as arguments.
	 *
	 * <ol>
	 * <li>zero or more <em>indexed arguments</em> come first.</li>
	 * <li>zero or more <em>aggregators</em> come next.</li>
	 * <li>zero or more arguments supplied by other {@code ParameterResolver}
	 * implementations come last.</li>
	 * </ol>
	 *
	 * @return {@code true} if the method has a potentially valid signature
	 */
	public static boolean hasPotentiallyValidSignature(Method method) {
		Parameter[] parameters = method.getParameters();
		int indexOfPreviousAggregator = -1;
		for (int i = 0; i < parameters.length; i++) {
			if (isAggregator(parameters[i])) {
				if ((indexOfPreviousAggregator != -1) && (i != indexOfPreviousAggregator + 1)) {
					return false;
				}
				indexOfPreviousAggregator = i;
			}
		}
		return true;
	}

	/**
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	public static boolean isAggregator(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter, AggregateWith.class);
	}

	/**
	 * Determine if the supplied {@link Method} declares at least one
	 * {@link Parameter} that is an {@linkplain #isAggregator aggregator}.
	 *
	 * @return {@code true} if the method has an aggregator
	 */
	public static boolean hasAggregator(Method method) {
		return indexOfFirstAggregator(method) != -1;
	}

	/**
	 * Find the index of the first {@linkplain #isAggregator aggregator}
	 * {@link Parameter} in the supplied {@link Method}.
	 *
	 * @return the index of the first aggregator, or {@code -1} if not found
	 */
	public static int indexOfFirstAggregator(Method method) {
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			if (isAggregator(parameters[i])) {
				return i;
			}
		}
		return -1;
	}

}
