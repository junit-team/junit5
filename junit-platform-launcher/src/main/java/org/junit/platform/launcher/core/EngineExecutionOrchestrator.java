/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Orchestrates test execution using the configured test engines.
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7", consumers = "testkit")
public class EngineExecutionOrchestrator {

	private final TestExecutionListenerRegistry listenerRegistry;

	public EngineExecutionOrchestrator() {
		this(new TestExecutionListenerRegistry());
	}

	EngineExecutionOrchestrator(TestExecutionListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}

	void execute(InternalTestPlan internalTestPlan, TestExecutionListener... listeners) {
		LauncherDiscoveryResult discoveryResult = internalTestPlan.getDiscoveryResult();
		ConfigurationParameters configurationParameters = discoveryResult.getConfigurationParameters();
		TestExecutionListenerRegistry listenerRegistry = buildListenerRegistryForExecution(listeners);
		withInterceptedStreams(configurationParameters, listenerRegistry, testExecutionListener -> {
			testExecutionListener.testPlanExecutionStarted(internalTestPlan);
			execute(discoveryResult, new ExecutionListenerAdapter(internalTestPlan, testExecutionListener));
			testExecutionListener.testPlanExecutionFinished(internalTestPlan);
		});
	}

	private void withInterceptedStreams(ConfigurationParameters configurationParameters,
			TestExecutionListenerRegistry listenerRegistry, Consumer<TestExecutionListener> action) {

		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();
		Optional<StreamInterceptingTestExecutionListener> streamInterceptingTestExecutionListener = StreamInterceptingTestExecutionListener.create(
			configurationParameters, testExecutionListener::reportingEntryPublished);
		streamInterceptingTestExecutionListener.ifPresent(listenerRegistry::registerListeners);
		try {
			action.accept(testExecutionListener);
		}
		finally {
			streamInterceptingTestExecutionListener.ifPresent(StreamInterceptingTestExecutionListener::unregister);
		}
	}

	/**
	 * Executes tests for the supplied {@linkplain LauncherDiscoveryResult
	 * discovery results} and notifies the supplied {@linkplain
	 * EngineExecutionListener listener} of execution events.
	 */
	public void execute(LauncherDiscoveryResult discoveryResult, EngineExecutionListener listener) {
		for (TestEngine testEngine : discoveryResult.getTestEngines()) {
			TestDescriptor engineDescriptor = discoveryResult.getEngineTestDescriptor(testEngine);
			if (engineDescriptor instanceof EngineDiscoveryErrorDescriptor) {
				listener.executionStarted(engineDescriptor);
				listener.executionFinished(engineDescriptor,
					TestExecutionResult.failed(((EngineDiscoveryErrorDescriptor) engineDescriptor).getCause()));
			}
			else {
				execute(engineDescriptor, listener, discoveryResult.getConfigurationParameters(), testEngine);
			}
		}
	}

	private TestExecutionListenerRegistry buildListenerRegistryForExecution(TestExecutionListener... listeners) {
		if (listeners.length == 0) {
			return this.listenerRegistry;
		}
		TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry(this.listenerRegistry);
		registry.registerListeners(listeners);
		return registry;
	}

	private void execute(TestDescriptor engineDescriptor, EngineExecutionListener listener,
			ConfigurationParameters configurationParameters, TestEngine testEngine) {

		OutcomeDelayingEngineExecutionListener delayingListener = new OutcomeDelayingEngineExecutionListener(listener,
			engineDescriptor);
		try {
			testEngine.execute(new ExecutionRequest(engineDescriptor, delayingListener, configurationParameters));
			delayingListener.reportEngineOutcome();
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			delayingListener.reportEngineFailure(new JUnitException(
				String.format("TestEngine with ID '%s' failed to execute tests", testEngine.getId()), throwable));
		}
	}
}
