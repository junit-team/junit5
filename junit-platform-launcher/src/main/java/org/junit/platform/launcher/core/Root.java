/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.junit.platform.engine.Filter.composeFilters;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Represents the root of all discovered {@link TestEngine TestEngines} and
 * their {@link TestDescriptor TestDescriptors}.
 *
 * @since 1.0
 */
class Root {

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors = new LinkedHashMap<>(4);
	private final ConfigurationParameters configurationParameters;

	/**
	 * Maps to store list of {@link TestDescriptor} which are excluded.
	 * key: Reason of exclusion.
	 */
	private final Map<String, List<TestDescriptor>> exclusionReasonToDescriptorMap = new LinkedHashMap<>();

	Root(ConfigurationParameters configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	public ConfigurationParameters getConfigurationParameters() {
		return configurationParameters;
	}

	Map<String, List<TestDescriptor>> getExclusionReasonToDescriptorMap() {
		return exclusionReasonToDescriptorMap;
	}

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

	void applyPostDiscoveryFilters(LauncherDiscoveryRequest discoveryRequest) {
		Filter<TestDescriptor> postDiscoveryFilter = composeFilters(discoveryRequest.getPostDiscoveryFilters());
		TestDescriptor.Visitor removeExcludedTestDescriptors = descriptor -> {
			if (!descriptor.isRoot() && isExcluded(descriptor, postDiscoveryFilter)) {
				descriptor.removeFromHierarchy();
			}
		};
		acceptInAllTestEngines(removeExcludedTestDescriptors);
	}

	/**
	 * Prune all branches in the tree of {@link TestDescriptor TestDescriptors}
	 * that do not have executable tests.
	 *
	 * <p>If a {@link TestEngine} ends up with no {@code TestDescriptors} after
	 * pruning, it will <strong>not</strong> be removed.
	 */
	void prune() {
		acceptInAllTestEngines(TestDescriptor::prune);
	}

	private boolean isExcluded(TestDescriptor descriptor, Filter<TestDescriptor> postDiscoveryFilter) {
		FilterResult filterResult = postDiscoveryFilter.apply(descriptor);
		boolean excluded = descriptor.getChildren().isEmpty() && filterResult.excluded();
		if (excluded) {
			exclusionReasonToDescriptorMap.computeIfAbsent(filterResult.getReason().orElse("Unknown"),
				list -> new LinkedList<>()).add(descriptor);
		}
		return excluded;
	}

	private void acceptInAllTestEngines(TestDescriptor.Visitor visitor) {
		this.testEngineDescriptors.values().forEach(descriptor -> descriptor.accept(visitor));
	}

}
