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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.discoveryNEW.DiscoverySelectorResolver;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.support.hierarchical.HierarchicalTestEngine;

@API(Experimental)
public class JUnit5TestEngine extends HierarchicalTestEngine<JUnit5EngineExecutionContext> {

	public static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return ENGINE_ID;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		Preconditions.notNull(discoveryRequest, "discovery request must not be null");
		JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(uniqueId);
		resolveDiscoveryRequest(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveDiscoveryRequest(EngineDiscoveryRequest discoveryRequest,
			JUnit5EngineDescriptor engineDescriptor) {
		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
		resolver.resolveSelectors(discoveryRequest, engineDescriptor);
		//		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver(engineDescriptor);
		//		resolver.resolveSelectors(discoveryRequest);
		applyDiscoveryFilters(discoveryRequest, engineDescriptor);
	}

	private void applyDiscoveryFilters(EngineDiscoveryRequest discoveryRequest,
			JUnit5EngineDescriptor engineDescriptor) {
		new DiscoveryFilterApplier().applyAllFilters(discoveryRequest, engineDescriptor);
	}

	@Override
	protected JUnit5EngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JUnit5EngineExecutionContext(request.getEngineExecutionListener());
	}

}
