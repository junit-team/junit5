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
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.DiscoverySelectorResolver;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;

public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {

	public static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public JUnit5EngineDescriptor discoverTests(DiscoveryRequest specification) {
		Preconditions.notNull(specification, "specification must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(this);
		resolveSpecification(specification, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveSpecification(DiscoveryRequest specification, JUnit5EngineDescriptor engineDescriptor) {
		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver(engineDescriptor);
		for (DiscoverySelector element : specification.getSelectors()) {
			resolver.resolveElement(element);
		}
		applyEngineFilters(specification, engineDescriptor);
	}

	private void applyEngineFilters(DiscoveryRequest specification, JUnit5EngineDescriptor engineDescriptor) {
		List<ClassFilter> classFilters = specification.getFilterByType(ClassFilter.class);
		if (classFilters.isEmpty()) {
			return;
		}

		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor instanceof ClassTestDescriptor) {
				Class<?> testClass = ((ClassTestDescriptor) descriptor).getTestClass();

				// @formatter:off
				if (classFilters.stream()
						.map(filter -> filter.filter(testClass))
						.anyMatch(FilterResult::isFiltered)) {
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
