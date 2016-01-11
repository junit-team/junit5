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

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.HierarchicalTestEngine;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.SpecificationResolver;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;

public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {

	public static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public JUnit5EngineDescriptor discoverTests(TestPlanSpecification specification) {
		Preconditions.notNull(specification, "specification must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(this);
		resolveSpecification(specification, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveSpecification(TestPlanSpecification specification, JUnit5EngineDescriptor engineDescriptor) {
		SpecificationResolver resolver = new SpecificationResolver(engineDescriptor);
		for (TestPlanSpecificationElement element : specification) {
			resolver.resolveElement(element);
		}
		applyEngineFilters(specification, engineDescriptor);
	}

	private void applyEngineFilters(TestPlanSpecification specification, JUnit5EngineDescriptor engineDescriptor) {
		if (specification.getEngineFilters(ClassFilter.class).isEmpty()) {
			return;
		}
		ClassFilter filter = specification.getClassFilter();
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
}
