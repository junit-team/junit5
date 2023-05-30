/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.platform.engine.reporting.OutputDirProvider;

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
	private final OutputDirProvider outputDirProvider;

	@API(status = DEPRECATED, since = "1.11")
	@Deprecated
	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this(rootTestDescriptor, engineExecutionListener, configurationParameters, OutputDirProvider.NOOP);
	}

	private ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters, OutputDirProvider outputDirProvider) {
		this.rootTestDescriptor = rootTestDescriptor;
		this.engineExecutionListener = engineExecutionListener;
		this.configurationParameters = configurationParameters;
		this.outputDirProvider = outputDirProvider;
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
	@Deprecated
	@API(status = DEPRECATED, since = "1.11")
	public static ExecutionRequest create(TestDescriptor rootTestDescriptor,
			EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters) {
		return create(rootTestDescriptor, engineExecutionListener, configurationParameters, OutputDirProvider.NOOP);
	}

	/**
	 * Factory for creating an execution request.
	 *
	 * @param rootTestDescriptor the engine's root {@link TestDescriptor}; must
	 * not be {@code null}
	 * @param engineExecutionListener the {@link EngineExecutionListener} to be
	 * notified of test execution events; must not be {@code null}
	 * @param configurationParameters {@link ConfigurationParameters} that the
	 * engine may use to influence test execution; must not be {@code null}
	 * @param outputDirProvider the provider for test output directories; must
	 * not be {@code null}
	 * @return a new {@code ExecutionRequest}; never {@code null}
	 * @since 1.11
	 */
	@API(status = EXPERIMENTAL, since = "1.11")
	public static ExecutionRequest create(TestDescriptor rootTestDescriptor,
			EngineExecutionListener engineExecutionListener, ConfigurationParameters configurationParameters,
			OutputDirProvider outputDirProvider) {
		return new ExecutionRequest(rootTestDescriptor, engineExecutionListener, configurationParameters,
			outputDirProvider);
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

	public OutputDirProvider getOutputDirProvider() {
		return outputDirProvider;
	}

}
