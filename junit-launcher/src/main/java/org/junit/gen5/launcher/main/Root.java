/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static org.junit.gen5.engine.Filter.composeFilters;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.gen5.engine.*;
import org.junit.gen5.launcher.*;

/**
 * Represents the root of all discovered {@link TestEngine} and their {@link TestDescriptor}s.
 */
class Root {

	private static final TestDescriptor.Visitor REMOVE_DESCRIPTORS_WITHOUT_TESTS = descriptor -> {
		if (!descriptor.isRoot() && !descriptor.hasTests())
			descriptor.removeFromHierarchy();
	};

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors = new LinkedHashMap<>();

	/**
	 * Add an {@code engine}'s discovered root {@code testDescriptor}.
	 */
	void add(TestEngine engine, TestDescriptor testDescriptor) {
		testEngineDescriptors.put(engine, testDescriptor);
	}

	Iterable<TestEngine> getTestEngines() {
		return testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineDescriptors() {
		return testEngineDescriptors.values();
	}

	TestDescriptor getTestDescriptorFor(TestEngine testEngine) {
		return testEngineDescriptors.get(testEngine);
	}

	void applyPostDiscoveryFilters(TestDiscoveryRequest discoveryRequest) {
		Filter<TestDescriptor> postDiscoveryFilter = composeFilters(discoveryRequest.getPostDiscoveryFilters());
		TestDescriptor.Visitor removeExcludedTests = descriptor -> {
			if (isExcludedTest(descriptor, postDiscoveryFilter)) {
				descriptor.removeFromHierarchy();
			}
		};
		acceptInAllTestEngines(removeExcludedTests);
	}

	/**
	 * Prune all branches in the tree of {@link TestDescriptor} that do not have executable tests.
	 * If a {@link TestEngine} ends up with no {@link TestDescriptor}s after pruning, it will be removed.
	 */
	void prune() {
		acceptInAllTestEngines(REMOVE_DESCRIPTORS_WITHOUT_TESTS);
		pruneEmptyTestEngines();
	}

	private boolean isExcludedTest(TestDescriptor descriptor, Filter<TestDescriptor> postDiscoveryFilter) {
		return descriptor.isTest() && postDiscoveryFilter.filter(descriptor).excluded();
	}

	private void acceptInAllTestEngines(TestDescriptor.Visitor visitor) {
		testEngineDescriptors.values().stream().forEach(testEngine -> testEngine.accept(visitor));
	}

	private void pruneEmptyTestEngines() {
		testEngineDescriptors.values().removeIf(testEngine -> testEngine.getChildren().isEmpty());
	}

}
