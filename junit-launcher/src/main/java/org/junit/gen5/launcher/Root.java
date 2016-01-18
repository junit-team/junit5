/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptor.Visitor;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
final class Root {
	private final Map<TestEngine, TestDescriptor> testEngines = new LinkedHashMap<>();

	public void add(TestEngine engine, TestDescriptor testDescriptor) {
		testEngines.put(engine, testDescriptor);
	}

	public Collection<TestDescriptor> getAllTestDescriptors() {
		return testEngines.values();
	}

	Iterable<TestEngine> getTestEngines() {
		return testEngines.keySet();
	}

	TestDescriptor getTestDescriptorFor(TestEngine testEngine) {
		return testEngines.get(testEngine);
	}

	void applyFilters(DiscoveryRequest discoveryRequest) {
		Visitor filteringVisitor = (descriptor, remove) -> {
			if (!descriptor.isTest())
				return;
			if (!discoveryRequest.acceptDescriptor(descriptor))
				remove.run();
		};
		accept(filteringVisitor);
	}

	void accept(Visitor visitor) {
		testEngines.values().stream().forEach(testEngine -> testEngine.accept(visitor));
	}

	void prune() {
		Visitor pruningVisitor = (descriptor, remove) -> {
			if (descriptor.isRoot() || descriptor.hasTests())
				return;
			remove.run();
		};
		accept(pruningVisitor);
		testEngines.values().removeIf(testEngine -> testEngine.getChildren().isEmpty());
	}
}
