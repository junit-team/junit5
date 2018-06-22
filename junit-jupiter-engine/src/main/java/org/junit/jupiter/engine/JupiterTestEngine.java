/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

/**
 * The JUnit Jupiter {@link org.junit.platform.engine.TestEngine TestEngine}.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public final class JupiterTestEngine extends HierarchicalTestEngine<JupiterEngineExecutionContext> {

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
		new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
		ConfigurationParameters config = request.getConfigurationParameters();
		if (config.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false)) {
			return new ForkJoinPoolHierarchicalTestExecutorService(
				new PrefixedConfigurationParameters(config, PARALLEL_CONFIG_PREFIX));
		}
		return super.createExecutorService(request);
	}

	@Override
	protected JupiterEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new JupiterEngineExecutionContext(request.getEngineExecutionListener(),
			request.getConfigurationParameters());
	}

}
