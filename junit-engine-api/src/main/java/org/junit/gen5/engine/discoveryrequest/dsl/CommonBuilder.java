/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discoveryrequest.dsl;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.FilterResult;

/**
 * A collection of common builders for {@link DiscoveryRequest} elements.
 *
 * @since 5.0
 */
public class CommonBuilder {
	public static <T> DiscoveryFilter<T> alwaysIncluded() {
		return (any) -> FilterResult.included("Always included");
	}

	public static <T> DiscoveryFilter<T> alwaysExcluded() {
		return (any) -> FilterResult.excluded("Always excluded");
	}

	public static <T> DiscoveryFilter<T> allOf(DiscoveryFilter<T>... filters) {
		if (filters == null) {
			return alwaysIncluded();
		}
		else {
			return allOf(asList(filters));
		}
	}

	public static <T> DiscoveryFilter<T> allOf(Collection<DiscoveryFilter<T>> filters) {
		if (filters == null || filters.isEmpty()) {
			return alwaysIncluded();
		}
		else if (filters.size() == 1) {
			return filters.iterator().next();
		}
		else {
			return new AndAllDiscoveryFilter(filters);
		}
	}

	private static class AndAllDiscoveryFilter<T> implements DiscoveryFilter<T> {
		private final Collection<DiscoveryFilter<T>> filters;

		public AndAllDiscoveryFilter(Collection<DiscoveryFilter<T>> filters) {
			this.filters = filters;
		}

		@Override
		public FilterResult filter(T element) {
			// @formatter:off
            return this.filters.stream()
                    .map(filter -> filter.filter(element))
                    .filter(FilterResult::excluded)
                    .findFirst()
                    .orElse(FilterResult.included("Element was included by all filters."));
            // @formatter:on
		}

		@Override
		public String toString() {
			return this.filters.stream().map(Object::toString).map(s -> format("(%s)", s)).collect(joining(" and "));
		}
	}
}
