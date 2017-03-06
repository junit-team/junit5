/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
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
		filteredTestClasses.computeIfAbsent(testClass, key -> new LinkedList<>()).add(filter);
	}

	Set<TestClassRequest> toRequests(Predicate<? super Class<?>> predicate) {
		// @formatter:off
		return concat(completeRequests(predicate), filteredRequests(predicate))
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	private Stream<TestClassRequest> completeRequests(Predicate<? super Class<?>> predicate) {
		return completeTestClasses.stream().filter(predicate).map(TestClassRequest::new);
	}

	private Stream<TestClassRequest> filteredRequests(Predicate<? super Class<?>> predicate) {
		// @formatter:off
		return filteredTestClasses.entrySet()
				.stream()
				.filter(where(Entry::getKey, testClass -> !completeTestClasses.contains(testClass)))
				.filter(where(Entry::getKey, predicate))
				.map(entry -> new TestClassRequest(entry.getKey(), entry.getValue()));
		// @formatter:on
	}

}
