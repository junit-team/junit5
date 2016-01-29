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
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.resolver.EngineResolver;
import org.junit.gen5.engine.junit5.resolver.PreconfiguredTestResolverRegistry;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.engine.support.hierarchical.HierarchicalTestEngine;

public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {

	public static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "discovery request must not be null");
		EngineDescriptor engineDescriptor = EngineResolver.resolveEngine(this);
		new PreconfiguredTestResolverRegistry().notifyResolvers(engineDescriptor, discoveryRequest);
		new DiscoveryFilterApplier().applyAllFilters(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	protected JUnit5EngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JUnit5EngineExecutionContext(request.getEngineExecutionListener());
	}
}
