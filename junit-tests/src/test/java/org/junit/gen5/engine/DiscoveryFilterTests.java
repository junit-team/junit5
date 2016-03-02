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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.engine.FilterResult.excluded;
import static org.junit.gen5.engine.FilterResult.included;

import java.util.StringJoiner;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.launcher.DiscoveryFilterStub;

class DiscoveryFilterTests {

	@SuppressWarnings("unchecked")
	@Test
	void combineWithoutFilter() {
		DiscoveryFilter<Object>[] noFilters = new DiscoveryFilter[0];
		DiscoveryFilter<Object> combinedFilter = DiscoveryFilter.combine(noFilters);

		assertTrue(combinedFilter.filter(String.class).included());
		assertTrue(combinedFilter.filter(Object.class).included());
	}

	@Test
	void combineWithSingleFilter() {
		DiscoveryFilter<Class<?>> singleFilter = ClassFilter.byNamePattern(".*ring.*");
		DiscoveryFilter<Class<?>> combined = DiscoveryFilter.combine(singleFilter);
		assertSame(singleFilter, combined);
	}

	@Test
	void combineWithMultipleFiltersIsConjunction() {
		DiscoveryFilter<Class<?>> firstFilter = ClassFilter.byNamePattern(".*ring.*");
		DiscoveryFilter<Class<?>> secondFilter = ClassFilter.byNamePattern(".*Join.*");

		DiscoveryFilter<Class<?>> combined = DiscoveryFilter.combine(firstFilter, secondFilter);

		assertFalse(combined.filter(String.class).included());
		assertTrue(combined.filter(StringJoiner.class).included());
	}

	@Test
	void combineWithMultipleFiltersHasReadableDescription() {
		DiscoveryFilter<Object> firstFilter = new DiscoveryFilterStub(o -> excluded("wrong"), () -> "1st");
		DiscoveryFilter<Object> secondFilter = new DiscoveryFilterStub(o -> included("right"), () -> "2nd");

		DiscoveryFilter<Object> combined = DiscoveryFilter.combine(firstFilter, secondFilter);

		assertFalse(combined.filter(String.class).included());
		assertEquals("(1st) and (2nd)", combined.toString());
	}
}
