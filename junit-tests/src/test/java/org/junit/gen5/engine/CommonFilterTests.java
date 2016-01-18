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

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.FilterResult.excluded;
import static org.junit.gen5.engine.FilterResult.included;

import java.util.StringJoiner;

import org.junit.gen5.api.Test;

public class CommonFilterTests {
	@Test
	void allOfWithoutFilter() {
		DiscoveryFilter<Object>[] noFilters = new DiscoveryFilter[0];
		DiscoveryFilter<Object> combinedFilter = CommonFilter.combine(noFilters);

		assertTrue(combinedFilter.filter(String.class).included());
		assertTrue(combinedFilter.filter(Object.class).included());
	}

	@Test
	void allOfWithSingleFilter() {
		DiscoveryFilter<Class<?>> singleFilter = ClassFilter.byNamePattern(".*ring.*");
		DiscoveryFilter<Class<?>> combined = CommonFilter.combine(singleFilter);
		assertSame(singleFilter, combined);
	}

	@Test
	void allOfWithMultipleFiltersIsConjunction() {
		DiscoveryFilter<Class<?>> firstFilter = ClassFilter.byNamePattern(".*ring.*");
		DiscoveryFilter<Class<?>> secondFilter = ClassFilter.byNamePattern(".*Join.*");

		DiscoveryFilter<Class<?>> combined = CommonFilter.combine(firstFilter, secondFilter);

		assertFalse(combined.filter(String.class).included());
		assertTrue(combined.filter(StringJoiner.class).included());
	}

	@Test
	void allOfWithMultipleFiltersHasReadableDescription() {
		DiscoveryFilter<Object> firstFilter = new DiscoveryFilterMock(o -> excluded("wrong"), () -> "1st");
		DiscoveryFilter<Object> secondFilter = new DiscoveryFilterMock(o -> included("right"), () -> "2nd");

		DiscoveryFilter<Object> combined = CommonFilter.combine(firstFilter, secondFilter);

		assertFalse(combined.filter(String.class).included());
		assertEquals("(1st) and (2nd)", combined.toString());
	}
}
