/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import org.junit.gen5.commons.meta.API;

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
 * @since 5.0
 */
@API(Experimental)
public class ExecutionRequest {

	private final TestDescriptor rootTestDescriptor;

	private final EngineExecutionListener engineExecutionListener;

	private final ConfigurationParameters configurationParameters;

	@API(Internal)
	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener,
			ConfigurationParameters configurationParameters) {
		this.rootTestDescriptor = rootTestDescriptor;
		this.engineExecutionListener = engineExecutionListener;
		this.configurationParameters = configurationParameters;
	}

	/**
	 * Get the root {@link TestDescriptor} of the engine that processes this
	 * request.
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
