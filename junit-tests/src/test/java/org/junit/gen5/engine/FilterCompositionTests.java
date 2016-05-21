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
import org.junit.gen5.launcher.FilterStub;

/**
 * Unit tests for {@link Filter#composeFilters}.
 *
 * {@link Filter#composeFilters} will delegate to {@linkplain CompositeFilter} under the hood.
 *
 * @since 5.0
 */
class FilterCompositionTests {

	@Test
	void composingNoFiltersCreatesFilterThatIncludesEverything() {
		Filter<Object> composedFilter = Filter.composeFilters();

		assertTrue(composedFilter.filter(String.class).included());
		assertTrue(composedFilter.filter(Object.class).included());
	}

	@Test
	void composingSingleFilterWillReturnTheOriginalOne() {
		Filter<Class<?>> singleFilter = ClassFilter.byNamePattern(".*ring.*");
		Filter<Class<?>> composed = Filter.composeFilters(singleFilter);
		assertSame(singleFilter, composed);
	}

	@Test
	void composingMultipleFiltersIsAConjunctionOfFilters() {
		Filter<Class<?>> firstFilter = ClassFilter.byNamePattern(".*ring.*");
		Filter<Class<?>> secondFilter = ClassFilter.byNamePattern(".*Join.*");

		Filter<Class<?>> composed = Filter.composeFilters(firstFilter, secondFilter);

		assertFalse(composed.filter(String.class).included());
		assertTrue(composed.filter(StringJoiner.class).included());
	}

	@Test
	void aFilterComposedOfMultipleFiltersHasReadableDescription() {
		Filter<Object> firstFilter = new FilterStub<>(o -> excluded("wrong"), () -> "1st");
		Filter<Object> secondFilter = new FilterStub<>(o -> included("right"), () -> "2nd");

		Filter<Object> composed = Filter.composeFilters(firstFilter, secondFilter);

		assertFalse(composed.filter(String.class).included());
		assertEquals("(1st) and (2nd)", composed.toString());
	}

}
