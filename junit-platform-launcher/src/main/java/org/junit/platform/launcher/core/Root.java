/*
 * Copyright 2015-2020 the original author or authors.
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.Filter;
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
	private final LauncherDiscoveryRequest discoveryRequest;
	private final SuiteDescriptor suiteDescriptor;

	Root(LauncherDiscoveryRequest discoveryRequest) {
		this(discoveryRequest, null);
	}

	Root(LauncherDiscoveryRequest discoveryRequest, SuiteDescriptor suiteDescriptor) {
		this.discoveryRequest = discoveryRequest;
		this.suiteDescriptor = suiteDescriptor;
	}

	public LauncherDiscoveryRequest getDiscoveryRequest() {
		return discoveryRequest;
	}

	public ConfigurationParameters getConfigurationParameters() {
		return discoveryRequest.getConfigurationParameters();
	}

	Optional<TestDescriptor> getSuiteDescriptor() {
		return Optional.ofNullable(suiteDescriptor);
	}

	/**
	 * Add an {@code engine}'s root {@link TestDescriptor}.
	 */
	void add(TestEngine engine, TestDescriptor testDescriptor) {
		getSuiteDescriptor().ifPresent(suiteDescriptor -> suiteDescriptor.addChild(testDescriptor));
		this.testEngineDescriptors.put(engine, testDescriptor);
	}

	Iterable<TestEngine> getTestEngines() {
		return this.testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineDescriptors() {
		return getSuiteDescriptor()
				.map(o -> (Collection<TestDescriptor>) Collections.singleton(o))
				.orElseGet(this.testEngineDescriptors::values);
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
		return descriptor.getChildren().isEmpty() && postDiscoveryFilter.apply(descriptor).excluded();
	}

	private void acceptInAllTestEngines(TestDescriptor.Visitor visitor) {
		this.testEngineDescriptors.values().forEach(descriptor -> descriptor.accept(visitor));
	}

}
