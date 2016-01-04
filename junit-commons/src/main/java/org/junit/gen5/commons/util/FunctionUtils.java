/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Collection of utilities for working with {@link Function Functions},
 * {@link Predicate Predicates}, etc.
 *
 * @since 5.0
 */
public class FunctionUtils {

	private FunctionUtils() {
		/* no-op */
	}

	/**
	 * Returns a predicate that first applies the specified function and then
	 * tests the specified predicate against the result of the function.
	 *
	 * @param function the function to apply
	 * @param predicate the predicate to test against the result of the function
	 */
	public static <T, V> Predicate<T> where(Function<T, V> function, Predicate<? super V> predicate) {
		Preconditions.notNull(function, "function must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");
		return input -> predicate.test(function.apply(input));
	}

}
