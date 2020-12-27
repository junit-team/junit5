/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * The JUnit Platform Suite {@link org.junit.platform.engine.TestEngine TestEngine}.
 *
 * @since 5.8
 */
@API(status = INTERNAL, since = "5.8")
public final class SuiteTestEngine implements TestEngine {

	@Override
	public String getId() {
		return SuiteEngineDescriptor.ENGINE_ID;
	}

	/**
	 * Returns {@code org.junit.platform.suite} as the group ID.
	 */
	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.junit.platform.suite");
	}

	/**
	 * Returns {@code junit-platform-suite-engine} as the artifact ID.
	 */
	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("junit-platform-suite-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		SuiteConfiguration configuration = new SuiteConfiguration(discoveryRequest.getConfigurationParameters());
		SuiteEngineDescriptor engineDescriptor = new SuiteEngineDescriptor(uniqueId, configuration);
		new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		SuiteEngineDescriptor suiteEngineDescriptor = (SuiteEngineDescriptor) request.getRootTestDescriptor();
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();

		engineExecutionListener.executionStarted(suiteEngineDescriptor);

		// @formatter:off
		suiteEngineDescriptor.getChildren()
				.stream()
				.map(SuiteTestDescriptor.class::cast)
				.forEach(executeSuite(engineExecutionListener));
		// @formatter:on
		engineExecutionListener.executionFinished(suiteEngineDescriptor, TestExecutionResult.successful());
	}

	private Consumer<SuiteTestDescriptor> executeSuite(EngineExecutionListener listener) {
		return descriptor -> {
			listener.executionStarted(descriptor);

			TestExecutionListener testExecutionListener = new EngineExecutionListenerAdaptor(descriptor, listener);
			Launcher launcher = descriptor.getLauncher();
			TestPlan testPlan = descriptor.getTestPlan();
			launcher.execute(testPlan, testExecutionListener);

			listener.executionFinished(descriptor, TestExecutionResult.successful());
		};
	}

}
