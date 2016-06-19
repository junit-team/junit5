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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.FilterResult.included;

import java.util.StringJoiner;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.ClassFilter;
import org.junit.platform.launcher.FilterStub;

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

		assertTrue(composedFilter.apply(String.class).included());
		assertTrue(composedFilter.apply(Object.class).included());
	}

	@Test
	void composingSingleFilterWillReturnTheOriginalOne() {
		Filter<Class<?>> singleFilter = ClassFilter.includeClassNamePattern(".*ring.*");
		Filter<Class<?>> composed = Filter.composeFilters(singleFilter);
		assertSame(singleFilter, composed);
	}

	@Test
	void composingMultipleFiltersIsAConjunctionOfFilters() {
		Filter<Class<?>> firstFilter = ClassFilter.includeClassNamePattern(".*ring.*");
		Filter<Class<?>> secondFilter = ClassFilter.includeClassNamePattern(".*Join.*");

		Filter<Class<?>> composed = Filter.composeFilters(firstFilter, secondFilter);

		assertFalse(composed.apply(String.class).included());
		assertTrue(composed.apply(StringJoiner.class).included());
	}

	@Test
	void aFilterComposedOfMultipleFiltersHasReadableDescription() {
		Filter<Object> firstFilter = new FilterStub<>(o -> excluded("wrong"), () -> "1st");
		Filter<Object> secondFilter = new FilterStub<>(o -> included("right"), () -> "2nd");

		Filter<Object> composed = Filter.composeFilters(firstFilter, secondFilter);

		assertFalse(composed.apply(String.class).included());
		assertEquals("(1st) and (2nd)", composed.toString());
	}

}
