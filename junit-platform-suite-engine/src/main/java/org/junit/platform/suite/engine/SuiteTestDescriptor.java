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

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * {@link TestDescriptor} for tests based on the JUnit Platform Launcher API.
 *
 * <h3>Default Display Names</h3>
 *
 * <p>The default display name is the simple name of the class.
 *
 * @since 1.8
 * @see SuiteDisplayName
 */
final class SuiteTestDescriptor extends AbstractTestDescriptor {

	static final String SEGMENT_TYPE = "suite";

	private final SuiteLauncherDiscoveryRequestBuilder requestBuilder;

	private TestPlan testPlan;
	private Launcher launcher;

	SuiteTestDescriptor(UniqueId id, Class<?> suiteClass, SuiteConfiguration configuration) {
		super(id, getSuiteDisplayName(suiteClass), ClassSource.from(suiteClass));
		// @formatter:off
		UniqueId requestingSuiteId = requireNoCycles(configuration).parentSuiteId()
				.map(parentId -> UniqueIdHelper.append(parentId, getUniqueId()))
				.orElseGet(this::getUniqueId);
		// @formatter:on
		SuiteLauncherDiscoveryRequestBuilder requestBuilder = new SuiteLauncherDiscoveryRequestBuilder();
		this.requestBuilder = requestBuilder.configureRequestingSuiteId(requestingSuiteId);
	}

	private SuiteConfiguration requireNoCycles(SuiteConfiguration configuration) {
		configuration.parentSuiteId().ifPresent(parentSuiteId -> {
			UniqueId fullSuiteId = UniqueIdHelper.append(parentSuiteId, getUniqueId());
			Supplier<String> message = () -> String.format(
				"Configuration error: The suite configuration may not contain a cycle [%s]", fullSuiteId);
			Preconditions.condition(!UniqueIdHelper.containCycle(fullSuiteId, SEGMENT_TYPE), message);
		});
		return configuration;
	}

	UniqueId uniqueIdInSuite(TestIdentifier testDescriptor) {
		Preconditions.notNull(testDescriptor, "uniqueId most not be null");
		UniqueId uniqueIdInTestPlan = UniqueId.parse(testDescriptor.getUniqueId());
		UniqueId uniqueIdInSuite = getUniqueId();
		return UniqueIdHelper.append(uniqueIdInSuite, uniqueIdInTestPlan);
	}

	SuiteTestDescriptor addDiscoveryRequestFrom(Class<?> testClass) {
		Preconditions.condition(testPlan == null, "discovery request can not be modified after discovery");
		requestBuilder.addRequestFrom(testClass);
		return this;
	}

	SuiteTestDescriptor addDiscoveryRequestFrom(UniqueId uniqueId) {
		Preconditions.condition(testPlan == null, "discovery request can not be modified after discovery");
		requestBuilder.addRequestFrom(uniqueId);
		return this;
	}

	void discover() {
		Preconditions.condition(testPlan == null, "discovery can only happen once");

		LauncherDiscoveryRequest request = requestBuilder.build();
		// @formatter:off
		LauncherConfig launcherConfig = LauncherConfig.builder()
				.enableTestExecutionListenerAutoRegistration(false)
				.enablePostDiscoveryFilterAutoRegistration(false)
				.build();
		// @formatter:on
		this.launcher = LauncherFactory.create(launcherConfig);
		this.testPlan = launcher.discover(request);

		addTestIdentifiersToSuite(testPlan, testIdentifier -> {
			UniqueId uniqueId = uniqueIdInSuite(testIdentifier);
			return new TestIdentifierAsTestDescriptor(uniqueId, testIdentifier);
		});
	}

	private void addTestIdentifiersToSuite(TestPlan testPlan,
			Function<TestIdentifier, TestDescriptor> createTestDescriptor) {
		// @formatter:off
		testPlan.getRoots()
				.stream()
				.map(testIdentifier -> adapTestIdentifier(testPlan, testIdentifier, createTestDescriptor))
				.forEach(this::addChild);
		// @formatter:on
	}

	private static TestDescriptor adapTestIdentifier(TestPlan testPlan, TestIdentifier testIdentifier,
			Function<TestIdentifier, TestDescriptor> createTestDescriptor) {
		TestDescriptor testDescriptor = createTestDescriptor.apply(testIdentifier);
		// @formatter:off
		testPlan.getChildren(testIdentifier)
				.stream()
				.map(childIdentifier -> adapTestIdentifier(testPlan, childIdentifier, createTestDescriptor))
				.forEach(testDescriptor::addChild);
		// @formatter:on
		return testDescriptor;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	TestPlan getTestPlan() {
		return testPlan;
	}

	Launcher getLauncher() {
		return launcher;
	}

	private static String getSuiteDisplayName(Class<?> testClass) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, SuiteDisplayName.class)
				.map(SuiteDisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElse(testClass.getSimpleName());
		// @formatter:on
	}

}
