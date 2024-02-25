/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder.request;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
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
	private final Boolean failIfNoTests;
	private final Class<?> suiteClass;

	private LauncherDiscoveryResult launcherDiscoveryResult;
	private SuiteLauncher launcher;

	SuiteTestDescriptor(UniqueId id, Class<?> suiteClass, ConfigurationParameters configurationParameters) {
		super(id, getSuiteDisplayName(suiteClass), ClassSource.from(suiteClass));
		this.configurationParameters = configurationParameters;
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
			"discovery request can not be modified after discovery");
		discoveryRequestBuilder.suite(suiteClass);
		return this;
	}

	SuiteTestDescriptor addDiscoveryRequestFrom(UniqueId uniqueId) {
		Preconditions.condition(launcherDiscoveryResult == null,
			"discovery request can not be modified after discovery");
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

	void execute(EngineExecutionListener parentEngineExecutionListener) {
		parentEngineExecutionListener.executionStarted(this);
		// #2838: The discovery result from a suite may have been filtered by
		// post discovery filters from the launcher. The discovery result should
		// be pruned accordingly
		LauncherDiscoveryResult discoveryResult = this.launcherDiscoveryResult.withRetainedEngines(
			getChildren()::contains);
		TestExecutionSummary summary = launcher.execute(discoveryResult, parentEngineExecutionListener);
		parentEngineExecutionListener.executionFinished(this, computeTestExecutionResult(summary));
	}

	private TestExecutionResult computeTestExecutionResult(TestExecutionSummary summary) {
		if (failIfNoTests && summary.getTestsFoundCount() == 0) {
			return TestExecutionResult.failed(new NoTestsDiscoveredException(suiteClass));
		}
		return TestExecutionResult.successful();
	}

	@Override
	public boolean mayRegisterTests() {
		// While a suite will not register new tests after discovery, we pretend
		// it does. This allows the suite to fail if not tests were discovered.
		// If not, the empty suite would be pruned.
		return true;
	}

}
