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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.AnnotationUtils;

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
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	public static boolean isAggregator(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| AnnotationUtils.isAnnotated(parameter, AggregateWith.class);
	}

	/**
	 * Determine if the supplied {@link Method} declares at least one
	 * {@link Parameter} that is an {@linkplain #isAggregator aggregator}.
	 *
	 * @return {@code true} if the method has an aggregator
	 */
	public static boolean hasAggregator(Method method) {
		return indexOfLastAggregator(method) != -1;
	}

	/**
	 * Find the index of the last {@linkplain #isAggregator aggregator}
	 * {@link Parameter} in the supplied {@link Method}.
	 *
	 * @return the index of the last aggregator, or {@code -1} if not found
	 */
	public static int indexOfLastAggregator(Method method) {
		Parameter[] parameters = method.getParameters();
		int parameterCount = parameters.length;
		for (int i = parameterCount - 1; i >= 0; i--) {
			if (isAggregator(parameters[i])) {
				return i;
			}
		}
		return -1;
	}

}
