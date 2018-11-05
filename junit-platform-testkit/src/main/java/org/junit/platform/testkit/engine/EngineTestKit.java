/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

/**
 * {@code EngineTestKit} provides support for {@linkplain #execute executing a
 * test plan} for a given {@link TestEngine} and then accessing the results via
 * {@linkplain ExecutionResults a fluent API} to verify the expected results.
 *
 * @since 1.4
 * @see #execute(TestEngine, EngineDiscoveryRequest)
 * @see ExecutionResults
 */
@API(status = EXPERIMENTAL, since = "1.4")
public final class EngineTestKit {

	/**
	 * Execute tests for a given {@link EngineDiscoveryRequest} using the
	 * provided {@link TestEngine}.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method.
	 *
	 * @param testEngine the {@code TestEngine} to use
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use
	 * @return the recorded {@code ExecutionResults}
	 */
	public static ExecutionResults execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		execute(testEngine, discoveryRequest, executionRecorder);
		return executionRecorder.getExecutionResults();
	}

	private static void execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {

		UniqueId engineUniqueId = UniqueId.forEngine(testEngine.getId());
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest, engineUniqueId);
		ExecutionRequest request = new ExecutionRequest(engineTestDescriptor, listener,
			discoveryRequest.getConfigurationParameters());
		testEngine.execute(request);
	}

	private EngineTestKit() {
		/* no-op */
	}

}
