/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

/**
 * The JUnit Jupiter {@link org.junit.platform.engine.TestEngine}.
 *
 * @since 5.0
 */
@API(Experimental)
public class JupiterTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {

	public static final String ENGINE_ID = "junit-jupiter";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	/**
	 * Returns {@code org.junit.jupiter} as the group ID.
	 */
	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.junit.jupiter");
	}

	/**
	 * Returns {@code junit-jupiter-engine} as the artifact ID.
	 */
	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("junit-jupiter-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId);
		resolveDiscoveryRequest(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	private void resolveDiscoveryRequest(EngineDiscoveryRequest discoveryRequest,
			JupiterEngineDescriptor engineDescriptor) {
		DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();
		resolver.resolveSelectors(discoveryRequest, engineDescriptor);
		applyDiscoveryFilters(discoveryRequest, engineDescriptor);
	}

	private void applyDiscoveryFilters(EngineDiscoveryRequest discoveryRequest,
			JupiterEngineDescriptor engineDescriptor) {
		new DiscoveryFilterApplier().applyAllFilters(discoveryRequest, engineDescriptor);
	}

	@Override
	protected JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
			request.getConfigurationParameters());
	}

}
