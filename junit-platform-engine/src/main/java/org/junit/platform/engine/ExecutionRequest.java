/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * Provides a single {@link TestEngine} access to the information necessary to
 * execute its tests.
 *
 * <p>A request contains an engine's root {@link TestDescriptor}, the
 * {@link EngineExecutionListener} to be notified of test execution events, and
 * {@link ConfigurationParameters} that the engine may use to influence test
 * execution.
 *
 * @since 1.0
 * @see TestEngine
 */
@API(status = STABLE, since = "1.0")
public class ExecutionRequest {

	private final TestDescriptor rootTestDescriptor;

	private final EngineExecutionListener engineExecutionListener;

	private final ConfigurationParameters configurationParameters;

	@API(status = INTERNAL, since = "1.0")
	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this.rootTestDescriptor = rootTestDescriptor;
		this.engineExecutionListener = engineExecutionListener;
		this.configurationParameters = configurationParameters;
	}

	/**
	 * Factory for creating an execution request.
	 *
	 * @param rootTestDescriptor the engine's root {@link TestDescriptor}
	 * @param engineExecutionListener the {@link EngineExecutionListener} to be
	 * notified of test execution events
	 * @param configurationParameters {@link ConfigurationParameters} that the
	 * engine may use to influence test execution
	 * @return a new {@code ExecutionRequest}; never {@code null}
	 * @since 1.9
	 */
	@API(status = STABLE, since = "1.9")
	public static ExecutionRequest create(TestDescriptor rootTestDescriptor,
			EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters) {
		return new ExecutionRequest(rootTestDescriptor, engineExecutionListener, configurationParameters);
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
