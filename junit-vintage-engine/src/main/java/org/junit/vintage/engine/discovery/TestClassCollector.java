/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.stream.Stream.concat;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @since 4.12
 */
class TestClassCollector {

	private final Set<Class<?>> completeTestClasses = new LinkedHashSet<>();
	private final Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = new LinkedHashMap<>();

	void addCompletely(Class<?> testClass) {
		completeTestClasses.add(testClass);
	}

	void addFiltered(Class<?> testClass, RunnerTestDescriptorAwareFilter filter) {
		filteredTestClasses.computeIfAbsent(testClass, key -> new ArrayList<>()).add(filter);
	}

	Stream<TestClassRequest> toRequests() {
		return concat(completeRequests(), filteredRequests());
	}

	private Stream<TestClassRequest> completeRequests() {
		return completeTestClasses.stream().map(TestClassRequest::new);
	}

	private Stream<TestClassRequest> filteredRequests() {
		// @formatter:off
		return filteredTestClasses.entrySet()
				.stream()
				.filter(where(Entry::getKey, testClass -> !completeTestClasses.contains(testClass)))
				.map(entry -> new TestClassRequest(entry.getKey(), entry.getValue()));
		// @formatter:on
	}

}
