/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Default implementation of the {@link Launcher} API.
 *
 * <p>External clients can obtain an instance by invoking
 * {@link LauncherFactory#create()}.
 *
 * @since 1.0
 * @see Launcher
 * @see LauncherFactory
 */
class DefaultLauncher implements Launcher {

	private static final Logger logger = LoggerFactory.getLogger(DefaultLauncher.class);

	private final TestExecutionListenerRegistry listenerRegistry = new TestExecutionListenerRegistry();
	private final EngineDiscoveryResultValidator discoveryResultValidator = new EngineDiscoveryResultValidator();
	private final Iterable<TestEngine> testEngines;

	/**
	 * Construct a new {@code DefaultLauncher} with the supplied test engines.
	 *
	 * @param testEngines the test engines to delegate to; never {@code null} or empty
	 */
	DefaultLauncher(Iterable<TestEngine> testEngines) {
		Preconditions.condition(testEngines != null && testEngines.iterator().hasNext(),
			() -> "Cannot create Launcher without at least one TestEngine; "
					+ "consider adding an engine implementation JAR to the classpath");
		this.testEngines = validateEngineIds(testEngines);
	}

	private static Iterable<TestEngine> validateEngineIds(Iterable<TestEngine> testEngines) {
		Set<String> ids = new HashSet<>();
		for (TestEngine testEngine : testEngines) {
			// check usage of reserved id prefix
			if (!validateReservedIds(testEngine)) {
				logger.warn(() -> String.format(
					"Third-party TestEngine implementations are forbidden to use the reserved 'junit-' prefix for their ID: '%s'",
					testEngine.getId()));
			}

			// check uniqueness
			if (!ids.add(testEngine.getId())) {
				throw new JUnitException(String.format(
					"Cannot create Launcher for multiple engines with the same ID '%s'.", testEngine.getId()));
			}
		}
		return testEngines;
	}

	// https://github.com/junit-team/junit5/issues/1557
	private static boolean validateReservedIds(TestEngine testEngine) {
		String engineId = testEngine.getId();
		if (!engineId.startsWith("junit-")) {
			return true;
		}
		if (engineId.equals("junit-jupiter")) {
			validateWellKnownClassName(testEngine, "org.junit.jupiter.engine.JupiterTestEngine");
			return true;
		}
		if (engineId.equals("junit-vintage")) {
			validateWellKnownClassName(testEngine, "org.junit.vintage.engine.VintageTestEngine");
			return true;
		}
		return false;
	}

	private static void validateWellKnownClassName(TestEngine testEngine, String expectedClassName) {
		String actualClassName = testEngine.getClass().getName();
		if (actualClassName.equals(expectedClassName)) {
			return;
		}
		throw new JUnitException(
			String.format("Third-party TestEngine '%s' is forbidden to use the reserved '%s' TestEngine ID.",
				actualClassName, testEngine.getId()));
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		Preconditions.notEmpty(listeners, "listeners array must not be null or empty");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		this.listenerRegistry.registerListeners(listeners);
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		return InternalTestPlan.from(discoverRoot(discoveryRequest, "discovery"));
	}

	@Override
	public void execute(LauncherDiscoveryRequest discoveryRequest, TestExecutionListener... listeners) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		execute(InternalTestPlan.from(discoverRoot(discoveryRequest, "execution")), listeners);
	}

	@Override
	public void execute(TestPlan testPlan, TestExecutionListener... listeners) {
		Preconditions.notNull(testPlan, "TestPlan must not be null");
		Preconditions.condition(testPlan instanceof InternalTestPlan, "TestPlan was not returned by this Launcher");
		Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		execute((InternalTestPlan) testPlan, listeners);
	}

	TestExecutionListenerRegistry getTestExecutionListenerRegistry() {
		return listenerRegistry;
	}

	private Root discoverRoot(LauncherDiscoveryRequest discoveryRequest, String phase) {
		Root root = new Root(discoveryRequest.getConfigurationParameters());

		for (TestEngine testEngine : this.testEngines) {
			// @formatter:off
			boolean engineIsExcluded = discoveryRequest.getEngineFilters().stream()
					.map(engineFilter -> engineFilter.apply(testEngine))
					.anyMatch(FilterResult::excluded);
			// @formatter:on

			if (engineIsExcluded) {
				logger.debug(() -> String.format(
					"Test discovery for engine '%s' was skipped due to an EngineFilter in phase '%s'.",
					testEngine.getId(), phase));
				continue;
			}

			logger.debug(() -> String.format("Discovering tests during Launcher %s phase in engine '%s'.", phase,
				testEngine.getId()));

			TestDescriptor rootDescriptor = discoverEngineRoot(testEngine, discoveryRequest);
			root.add(testEngine, rootDescriptor);
		}
		root.applyPostDiscoveryFilters(discoveryRequest);
		root.prune();
		return root;
	}

	private TestDescriptor discoverEngineRoot(TestEngine testEngine, LauncherDiscoveryRequest discoveryRequest) {
		LauncherDiscoveryListener discoveryListener = discoveryRequest.getDiscoveryListener();
		UniqueId uniqueEngineId = UniqueId.forEngine(testEngine.getId());
		try {
			discoveryListener.engineDiscoveryStarted(uniqueEngineId);
			TestDescriptor engineRoot = testEngine.discover(discoveryRequest, uniqueEngineId);
			discoveryResultValidator.validate(testEngine, engineRoot);
			discoveryListener.engineDiscoveryFinished(uniqueEngineId, EngineDiscoveryResult.successful());
			return engineRoot;
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			String message = String.format("TestEngine with ID '%s' failed to discover tests", testEngine.getId());
			JUnitException cause = new JUnitException(message, throwable);
			discoveryListener.engineDiscoveryFinished(uniqueEngineId, EngineDiscoveryResult.failed(cause));
			return new EngineDiscoveryErrorDescriptor(uniqueEngineId, testEngine, cause);
		}
	}

	private void execute(InternalTestPlan internalTestPlan, TestExecutionListener[] listeners) {
		Root root = internalTestPlan.getRoot();
		ConfigurationParameters configurationParameters = root.getConfigurationParameters();
		TestExecutionListenerRegistry listenerRegistry = buildListenerRegistryForExecution(listeners);
		withInterceptedStreams(configurationParameters, listenerRegistry, testExecutionListener -> {
			testExecutionListener.testPlanExecutionStarted(internalTestPlan);
			ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(internalTestPlan,
				testExecutionListener);
			for (TestEngine testEngine : root.getTestEngines()) {
				TestDescriptor engineDescriptor = root.getTestDescriptorFor(testEngine);
				if (engineDescriptor instanceof EngineDiscoveryErrorDescriptor) {
					engineExecutionListener.executionStarted(engineDescriptor);
					engineExecutionListener.executionFinished(engineDescriptor,
						TestExecutionResult.failed(((EngineDiscoveryErrorDescriptor) engineDescriptor).getCause()));
				}
				else {
					execute(engineDescriptor, engineExecutionListener, configurationParameters, testEngine);
				}
			}
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
