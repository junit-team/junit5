/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class TestClassCollection {

	private final Set<Class<?>> completeTestClasses = new LinkedHashSet<>();
	private final Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = new LinkedHashMap<>();

	void addCompletely(Class<?> testClass) {
		completeTestClasses.add(testClass);
	}

	void addFiltered(Class<?> testClass, RunnerTestDescriptorAwareFilter filter) {
		filteredTestClasses.computeIfAbsent(testClass, key -> new LinkedList<>()).add(filter);
	}

	Set<TestClassEntry> toEntries() {
		return concat(completeEntries(), filteredEntries()).collect(toCollection(LinkedHashSet::new));
	}

	private Stream<TestClassEntry> completeEntries() {
		return completeTestClasses.stream().map(TestClassEntry::new);
	}

	private Stream<TestClassEntry> filteredEntries() {
		// TODO #40 Remove classes contained in completeTestClasses
		return filteredTestClasses.entrySet().stream().map(
			entry -> new TestClassEntry(entry.getKey(), entry.getValue()));
	}

	static class TestClassEntry {

		private final Class<?> testClass;
		private final List<RunnerTestDescriptorAwareFilter> filters;

		TestClassEntry(Class<?> testClass) {
			this(testClass, emptyList());
		}

		TestClassEntry(Class<?> testClass, List<RunnerTestDescriptorAwareFilter> filters) {
			this.testClass = testClass;
			this.filters = filters;
		}

		Class<?> getTestClass() {
			return testClass;
		}

		List<RunnerTestDescriptorAwareFilter> getFilters() {
			return filters;
		}

	}

}
