/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.junit.platform.engine.FilterResult.included;

import java.util.Collection;

/**
 * Combines a collection of {@link Filter Filters} into a new filter that will
 * include elements if and only if all of the filters in the specified collection
 * include it.
 *
 * @since 5.0
 */
class CompositeFilter<T> implements Filter<T> {

	@SuppressWarnings("rawtypes")
	private static final Filter ALWAYS_INCLUDED_FILTER = obj -> included("Always included");

	private static final FilterResult INCLUDED_BY_ALL_FILTERS = included("Element was included by all filters.");

	@SuppressWarnings("unchecked")
	static <T> Filter<T> alwaysIncluded() {
		return ALWAYS_INCLUDED_FILTER;
	}

	private final Collection<? extends Filter<T>> filters;

	CompositeFilter(Collection<? extends Filter<T>> filters) {
		this.filters = filters;
	}

	@Override
	public FilterResult apply(T element) {
		// @formatter:off
		return filters.stream()
				.map(filter -> filter.apply(element))
				.filter(FilterResult::excluded)
				.findFirst()
				.orElse(INCLUDED_BY_ALL_FILTERS);
		// @formatter:on
	}

	@Override
	public String toString() {
		return filters.stream().map(Object::toString).map(value -> format("(%s)", value)).collect(joining(" and "));
	}

}
