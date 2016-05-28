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

import org.junit.gen5.engine.Filter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Represents the root of all discovered {@link TestEngine TestEngines} and
 * their {@link TestDescriptor TestDescriptors}.
 *
 * @since 5.0
 */
class Root {

	private static final TestDescriptor.Visitor REMOVE_DESCRIPTORS_WITHOUT_TESTS = descriptor -> {
		if (!descriptor.isRoot() && !descriptor.hasTests()) {
			descriptor.removeFromHierarchy();
		}
	};

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors = new LinkedHashMap<>();

	/**
	 * Add an {@code engine}'s root {@link TestDescriptor}.
	 */
	void add(TestEngine engine, TestDescriptor testDescriptor) {
		this.testEngineDescriptors.put(engine, testDescriptor);
	}

	Iterable<TestEngine> getTestEngines() {
		return this.testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineDescriptors() {
		return this.testEngineDescriptors.values();
	}

	TestDescriptor getTestDescriptorFor(TestEngine testEngine) {
		return this.testEngineDescriptors.get(testEngine);
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
	 * Prune all branches in the tree of {@link TestDescriptor TestDescriptors}
	 * that do not have executable tests.
	 *
	 * <p>If a {@link TestEngine} ends up with no {@code TestDescriptors} after
	 * pruning, it will be removed.
	 */
	void prune() {
		acceptInAllTestEngines(REMOVE_DESCRIPTORS_WITHOUT_TESTS);
		pruneEmptyTestEngines();
	}

	private boolean isExcludedTest(TestDescriptor descriptor, Filter<TestDescriptor> postDiscoveryFilter) {
		return descriptor.isTest() && postDiscoveryFilter.filter(descriptor).excluded();
	}

	private void acceptInAllTestEngines(TestDescriptor.Visitor visitor) {
		this.testEngineDescriptors.values().forEach(descriptor -> descriptor.accept(visitor));
	}

	private void pruneEmptyTestEngines() {
		this.testEngineDescriptors.values().removeIf(descriptor -> descriptor.getChildren().isEmpty());
	}

}
