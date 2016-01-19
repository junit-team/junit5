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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.gen5.engine.*;
import org.junit.gen5.launcher.*;

class Root {
	private final Map<TestEngine, TestDescriptor> testEngineDescriptors = new LinkedHashMap<>();

	public void add(TestEngine engine, TestDescriptor testDescriptor) {
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
		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (!descriptor.isTest())
				return;

			if (isExcluded(discoveryRequest, descriptor)) {
				remove.run();
			}
		};
		accept(filteringVisitor);
	}

	private boolean isExcluded(TestDiscoveryRequest discoveryRequest, TestDescriptor descriptor) {
		// @formatter:off
		return discoveryRequest.getPostDiscoveryFilters().stream()
				.map(filter -> filter.filter(descriptor))
				.anyMatch(FilterResult::excluded);
		// @formatter:on
	}

	void accept(TestDescriptor.Visitor visitor) {
		testEngineDescriptors.values().stream().forEach(testEngine -> testEngine.accept(visitor));
	}

	void prune() {
		TestDescriptor.Visitor pruningVisitor = (descriptor, remove) -> {
			if (descriptor.isRoot() || descriptor.hasTests())
				return;
			remove.run();
		};
		accept(pruningVisitor);
		testEngineDescriptors.values().removeIf(testEngine -> testEngine.getChildren().isEmpty());
	}
}
