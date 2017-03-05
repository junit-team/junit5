/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with {@link Collection Collections}.
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
public final class CollectionUtils {

	///CLOVER:OFF
	private CollectionUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Read the only element of a collection of size 1.
	 *
	 * @param collection the collection to get the element from
	 * @return the only element of the collection
	 * @throws PreconditionViolationException if the collection is {@code null}
	 * or does not contain exactly one element
	 */
	public static <T> T getOnlyElement(Collection<T> collection) {
		Preconditions.notNull(collection, "collection must not be null");
		Preconditions.condition(collection.size() == 1,
			() -> "collection must contain exactly one element: " + collection);
		return collection.iterator().next();
	}

	/**
	 * Returns a {@code Collector} that accumulates the input elements into a
	 * new unmodifiable list, in encounter order.
	 *
	 * <p>There are no guarantees on the type or serializability of the list
	 * returned, so if more control over the returned list is required,
	 * consider creating a new {@code Collector} implementation like the
	 * following:
	 * <pre>{@code
	 *     public static <T> Collector<T, ?, List<T>> toUnmodifiableList(Supplier<List<T>> listSupplier) {
	 *         return collectingAndThen(toCollection(listSupplier), Collections::unmodifiableList);
	 *     }
	 * }</pre>
	 *
	 * @param <T> the type of the input elements
	 * @return a {@code Collector} which collects all the input elements into
	 * an unmodifiable list, in encounter order
	 */
	public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
		return collectingAndThen(toList(), Collections::unmodifiableList);
	}

}
