/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import org.junit.platform.commons.meta.API;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.platform.commons.meta.API.Usage.Internal;

/**
 * Collection of utilities for working with {@link Function Functions},
 * {@link Predicate Predicates}, etc.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
public final class FunctionUtils {

	///CLOVER:OFF
	private FunctionUtils() {
		/* no-op */
	}
	///CLOVER:ON

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

	/**
	 * Lazily executes the specified {@link Supplier}s until the first yields a non-empty {@link Optional};
	 * otherwise returns an {@link Optional#empty() empty} Optional.
	 *
	 * <p>apiNote: This works much like {@code Optional::or}, which is introduced in Java 9.</p>
	 *
	 * @param suppliers the suppliers producing Optional instances
	 * @throws PreconditionViolationException if the
	 */
	@SuppressWarnings({"varargs", "unchecked"})
	@SafeVarargs
	public static <T> Optional<T> firstPresent(Supplier<Optional<? extends T>>... suppliers) {
		Preconditions.notNull(suppliers, "suppliers must not be null");
		Optional<? extends T> firstPresent = Arrays.stream(suppliers)
				.map(Supplier::get)
				.peek(optional -> Preconditions.notNull(optional, "supplied optional must not be null"))
				.filter(Optional::isPresent)
				.findFirst()
				.orElse(Optional.empty());
		// this cast is safe because Optional is covariant,
		return (Optional<T>) firstPresent;
	}

}
