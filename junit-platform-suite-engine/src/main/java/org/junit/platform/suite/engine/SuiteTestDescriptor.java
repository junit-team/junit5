/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder;

/**
 * {@link TestDescriptor} for tests based on the JUnit Platform Suite API.
 *
 * <h2>Default Display Names</h2>
 *
 * <p>The default display name is the simple name of the class.
 *
 * @since 1.8
 * @see SuiteDisplayName
 */
final class SuiteTestDescriptor extends AbstractTestDescriptor {

	static final String SEGMENT_TYPE = "suite";

	private final SuiteLauncherDiscoveryRequestBuilder discoveryRequestBuilder = request();
	private final ConfigurationParameters configurationParameters;
	private final OutputDirectoryProvider outputDirectoryProvider;
	private final Boolean failIfNoTests;
	private final Class<?> suiteClass;

	private LauncherDiscoveryResult launcherDiscoveryResult;
	private SuiteLauncher launcher;

	SuiteTestDescriptor(UniqueId id, Class<?> suiteClass, ConfigurationParameters configurationParameters,
			OutputDirectoryProvider outputDirectoryProvider) {
		super(id, getSuiteDisplayName(suiteClass), ClassSource.from(suiteClass));
		this.configurationParameters = configurationParameters;
		this.outputDirectoryProvider = outputDirectoryProvider;
		this.failIfNoTests = getFailIfNoTests(suiteClass);
		this.suiteClass = suiteClass;
	}

	private static Boolean getFailIfNoTests(Class<?> suiteClass) {
		// @formatter:off
		return findAnnotation(suiteClass, Suite.class)
				.map(Suite::failIfNoTests)
				.orElseThrow(() -> new JUnitException(String.format("Suite [%s] was not annotated with @Suite", suiteClass.getName())));
		// @formatter:on
	}

	SuiteTestDescriptor addDiscoveryRequestFrom(Class<?> suiteClass) {
		Preconditions.condition(launcherDiscoveryResult == null,
			"discovery request cannot be modified after discovery");
		discoveryRequestBuilder.applySelectorsAndFiltersFromSuite(suiteClass);
		return this;
	}

	SuiteTestDescriptor addDiscoveryRequestFrom(UniqueId uniqueId) {
		Preconditions.condition(launcherDiscoveryResult == null,
			"discovery request cannot be modified after discovery");
		discoveryRequestBuilder.selectors(DiscoverySelectors.selectUniqueId(uniqueId));
		return this;
	}

	void discover() {
		if (launcherDiscoveryResult != null) {
			return;
		}

		// @formatter:off
		LauncherDiscoveryRequest request = discoveryRequestBuilder
				.filterStandardClassNamePatterns(true)
				.enableImplicitConfigurationParameters(false)
				.parentConfigurationParameters(configurationParameters)
				.applyConfigurationParametersFromSuite(suiteClass)
				.outputDirectoryProvider(outputDirectoryProvider)
				.build();
		// @formatter:on
		this.launcher = SuiteLauncher.create();
		this.launcherDiscoveryResult = launcher.discover(request, getUniqueId());
		// @formatter:off
		launcherDiscoveryResult.getTestEngines()
				.stream()
				.map(testEngine -> launcherDiscoveryResult.getEngineTestDescriptor(testEngine))
				.forEach(this::addChild);
		// @formatter:on
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	private static String getSuiteDisplayName(Class<?> testClass) {
		// @formatter:off
		return findAnnotation(testClass, SuiteDisplayName.class)
				.map(SuiteDisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElse(testClass.getSimpleName());
		// @formatter:on
	}

	void execute(EngineExecutionListener parentEngineExecutionListener,
			NamespacedHierarchicalStore<Namespace> requestLevelStore) {
		parentEngineExecutionListener.executionStarted(this);
		ThrowableCollector throwableCollector = new OpenTest4JAwareThrowableCollector();

		List<Method> beforeSuiteMethods = LifecycleMethodUtils.findBeforeSuiteMethods(suiteClass, throwableCollector);
		List<Method> afterSuiteMethods = LifecycleMethodUtils.findAfterSuiteMethods(suiteClass, throwableCollector);

		executeBeforeSuiteMethods(beforeSuiteMethods, throwableCollector);

		TestExecutionSummary summary = executeTests(parentEngineExecutionListener, requestLevelStore,
			throwableCollector);

		executeAfterSuiteMethods(afterSuiteMethods, throwableCollector);

		TestExecutionResult testExecutionResult = computeTestExecutionResult(summary, throwableCollector);
		parentEngineExecutionListener.executionFinished(this, testExecutionResult);
	}

	private void executeBeforeSuiteMethods(List<Method> beforeSuiteMethods, ThrowableCollector throwableCollector) {
		if (throwableCollector.isNotEmpty()) {
			return;
		}
		for (Method beforeSuiteMethod : beforeSuiteMethods) {
			throwableCollector.execute(() -> ReflectionSupport.invokeMethod(beforeSuiteMethod, null));
			if (throwableCollector.isNotEmpty()) {
				return;
			}
		}
	}

	private TestExecutionSummary executeTests(EngineExecutionListener parentEngineExecutionListener,
			NamespacedHierarchicalStore<Namespace> requestLevelStore, ThrowableCollector throwableCollector) {
		if (throwableCollector.isNotEmpty()) {
			return null;
		}

		// #2838: The discovery result from a suite may have been filtered by
		// post discovery filters from the launcher. The discovery result should
		// be pruned accordingly.
		LauncherDiscoveryResult discoveryResult = this.launcherDiscoveryResult.withRetainedEngines(
			getChildren()::contains);
		return launcher.execute(discoveryResult, parentEngineExecutionListener, requestLevelStore);
	}

	private void executeAfterSuiteMethods(List<Method> afterSuiteMethods, ThrowableCollector throwableCollector) {
		for (Method afterSuiteMethod : afterSuiteMethods) {
			throwableCollector.execute(() -> ReflectionSupport.invokeMethod(afterSuiteMethod, null));
		}
	}

	private TestExecutionResult computeTestExecutionResult(TestExecutionSummary summary,
			ThrowableCollector throwableCollector) {
		if (throwableCollector.isNotEmpty()) {
			return TestExecutionResult.failed(throwableCollector.getThrowable());
		}
		if (failIfNoTests && summary.getTestsFoundCount() == 0) {
			return TestExecutionResult.failed(new NoTestsDiscoveredException(suiteClass));
		}
		return TestExecutionResult.successful();
	}

	@Override
	public boolean mayRegisterTests() {
		// While a suite will not register new tests after discovery, we pretend
		// it does. This allows the suite to fail if no tests were discovered.
		// Otherwise, the empty suite would be pruned.
		return true;
	}

}
