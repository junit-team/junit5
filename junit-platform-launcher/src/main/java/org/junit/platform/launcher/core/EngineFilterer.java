/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.EngineFilter;

class EngineFilterer {

	private final List<EngineFilter> engineFilters;

	private final List<TestEngine> checkedTestEngines = new ArrayList<>();

	EngineFilterer(List<EngineFilter> engineFilters) {
		this.engineFilters = engineFilters;
	}

	boolean isExcluded(TestEngine testEngine) {
		checkedTestEngines.add(testEngine);
		return engineFilters.stream() //
				.map(engineFilter -> engineFilter.apply(testEngine)) //
				.anyMatch(FilterResult::excluded);
	}

	void checkNoUnmatchedIncludeFilter() {
		SortedSet<String> unmatchedEngineIdsOfIncludeFilters = getUnmatchedEngineIdsOfIncludeFilters();
		if (!unmatchedEngineIdsOfIncludeFilters.isEmpty()) {
			throw new JUnitException("No TestEngine ID matched the following include EngineFilters: "
					+ unmatchedEngineIdsOfIncludeFilters + ".\n" //
					+ "Please fix or remove the filter.\n" //
					+ TestEngineFormatter.format("Registered TestEngines", checkedTestEngines));
		}
	}

	private SortedSet<String> getUnmatchedEngineIdsOfIncludeFilters() {
		return engineFilters.stream() //
				.filter(EngineFilter::isIncludeFilter) //
				.filter(engineFilter -> checkedTestEngines.stream().map(engineFilter::apply).noneMatch(
					FilterResult::included)) //
				.flatMap(engineFilter -> engineFilter.getEngineIds().stream()) //
				.collect(toCollection(TreeSet::new));
	}
}
