/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.List;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.DiscoverySelectorResolver;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.support.hierarchical.HierarchicalTestEngine;

public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {

	public static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public JUnit5EngineDescriptor discoverTests(EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "discovery request must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(this);
		resolveDiscoveryRequest(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveDiscoveryRequest(EngineDiscoveryRequest discoveryRequest,
			JUnit5EngineDescriptor engineDescriptor) {
		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver(engineDescriptor);
		for (DiscoverySelector element : discoveryRequest.getSelectors()) {
			resolver.resolveElement(element);
		}
		applyDiscoveryFilters(discoveryRequest, engineDescriptor);
	}

	private void applyDiscoveryFilters(EngineDiscoveryRequest discoveryRequest,
			JUnit5EngineDescriptor engineDescriptor) {
		List<ClassFilter> classFilters = discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class);
		if (classFilters.isEmpty()) {
			return;
		}

		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor instanceof ClassTestDescriptor) {
				Class<?> testClass = ((ClassTestDescriptor) descriptor).getTestClass();

				// @formatter:off
				if (classFilters.stream()
						.map(filter -> filter.filter(testClass))
						.anyMatch(FilterResult::excluded)) {
					remove.run();
				}
				// @formatter:on
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	@Override
	protected JUnit5EngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JUnit5EngineExecutionContext(request.getEngineExecutionListener());
	}
}
