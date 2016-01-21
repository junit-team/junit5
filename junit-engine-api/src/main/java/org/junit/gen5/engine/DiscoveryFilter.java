/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.Arrays.asList;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.engine.CombinedDiscoveryFilter.alwaysIncluded;

import java.util.Collection;

/**
 * Filters particular tests during test discovery.
 *
 * <p>These filters need to be applied by the {@link TestEngine} during test
 * discovery.
 *
 * @since 5.0
 * @see EngineDiscoveryRequest
 * @see TestEngine
 */
public interface DiscoveryFilter<T> extends GenericFilter<T> {

	/**
	 * Combines an array of {@code DiscoveryFilters} into a new filter that will
	 * include elements if and only if all of the filters in the specified array
	 * include it.
	 *
	 * <p>If the array is {@code null} or empty, the returned filter will
	 * include all elements it is asked to filter.
	 *
	 * <p>If the length of the array is 1, this method will return the filter
	 * contained in the array.
	 */
	@SafeVarargs
	public static <T> DiscoveryFilter<T> combine(DiscoveryFilter<T>... filters) {
		if (filters == null) {
			return alwaysIncluded();
		}
		return combine(asList(filters));
	}

	/**
	 * Combines a collection of {@code DiscoveryFilters} into a new filter that
	 * will include elements if and only if all of the filters in the specified
	 * collection include it.
	 *
	 * <p>If the collection is {@code null} or empty, the returned filter will
	 * include all elements it is asked to filter.
	 *
	 * <p>If the size of the collection is 1, this method will return the filter
	 * contained in the collection.
	 */
	public static <T> DiscoveryFilter<T> combine(Collection<DiscoveryFilter<T>> filters) {
		if (filters == null || filters.isEmpty()) {
			return alwaysIncluded();
		}
		if (filters.size() == 1) {
			return getOnlyElement(filters);
		}
		return new CombinedDiscoveryFilter<>(filters);
	}

}
