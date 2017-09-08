/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Status.INTERNAL;
import static org.junit.platform.commons.meta.API.Status.STABLE;

import org.junit.platform.commons.meta.API;

/**
 * Provides a single {@link TestEngine} access to the information necessary to
 * execute its tests.
 *
 * <p>A request contains an engine's root {@link TestDescriptor}, the
 * {@link EngineExecutionListener} to be notified of test execution events, and
 * {@link ConfigurationParameters} that the engine may use to influence test
 * execution.
 *
 * @see TestEngine
 * @since 1.0
 */
@API(status = STABLE)
public class ExecutionRequest {

	private final TestDescriptor rootTestDescriptor;

	private final EngineExecutionListener engineExecutionListener;

	private final ConfigurationParameters configurationParameters;

	@API(status = INTERNAL)
	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this.rootTestDescriptor = rootTestDescriptor;
		this.engineExecutionListener = engineExecutionListener;
		this.configurationParameters = configurationParameters;
	}

	/**
	 * Get the root {@link TestDescriptor} of the engine that processes this
	 * request.
	 *
	 * <p><strong>Note</strong>: the <em>root</em> descriptor is the
	 * {@code TestDescriptor} returned by
	 * {@link TestEngine#discover(EngineDiscoveryRequest, UniqueId)}.
	 */
	public TestDescriptor getRootTestDescriptor() {
		return this.rootTestDescriptor;
	}

	/**
	 * Get the {@link EngineExecutionListener} to be notified of test execution
	 * events.
	 */
	public EngineExecutionListener getEngineExecutionListener() {
		return this.engineExecutionListener;
	}

	/**
	 * Get the {@link ConfigurationParameters} that the engine may use to
	 * influence test execution.
	 */
	public ConfigurationParameters getConfigurationParameters() {
		return this.configurationParameters;
	}

}
