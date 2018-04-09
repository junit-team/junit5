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
import java.util.Arrays;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * Collection of utilities for working with aggregating argument consumers
 * in parameterized tests.
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

	public static boolean isAggregate(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| AnnotationUtils.isAnnotated(parameter, AggregateWith.class);
	}

	public static boolean hasAggregate(Method method) {
		return Arrays.stream(method.getParameters()).anyMatch(AggregationUtils::isAggregate);
	}

	public static int indexOfLastAggregate(Method method) {
		int parameterCount = method.getParameterCount();
		for (int i = parameterCount - 1; i >= 0; i--) {
			if (isAggregate(method.getParameters()[i])) {
				return i;
			}
		}
		return -1;
	}

}
