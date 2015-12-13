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
import java.util.ServiceLoader;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.resolver.TestResolver;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistryImpl;

public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {
	private static final String ENGINE_ID = "junit5";

	private TestResolverRegistry testResolverRegistry;

	@Override
	public void initialize() {
		testResolverRegistry = new TestResolverRegistryImpl(this);
		ServiceLoader<TestResolver> serviceLoader = ServiceLoader.load(TestResolver.class);
		serviceLoader.forEach(testResolver -> testResolverRegistry.register(testResolver));
	}

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public JUnit5EngineDescriptor discoverTests(TestPlanSpecification specification) {
		Preconditions.notNull(specification, "specification must not be null");

		JUnit5EngineDescriptor root = new JUnit5EngineDescriptor(this);
		testResolverRegistry.notifyResolvers(root, specification);

		// TODO Rework filter mechanism
		applyEngineFilters(specification.getEngineFilters(), root);

		return root;
	}

	private void applyEngineFilters(List<EngineFilter> engineFilters, JUnit5EngineDescriptor engineDescriptor) {
		// TODO Currently only works with a single ClassFilter
		if (engineFilters.isEmpty()) {
			return;
		}
		ClassFilter filter = (ClassFilter) engineFilters.get(0);
		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor.getClass() == ClassTestDescriptor.class) {
				ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) descriptor;
				if (!filter.acceptClass(classTestDescriptor.getTestClass()))
					remove.run();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	@Override
	protected JUnit5EngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JUnit5EngineExecutionContext();
	}

	public void setTestResolverRegistry(TestResolverRegistry testResolverRegistry) {
		this.testResolverRegistry = testResolverRegistry;
	}
}