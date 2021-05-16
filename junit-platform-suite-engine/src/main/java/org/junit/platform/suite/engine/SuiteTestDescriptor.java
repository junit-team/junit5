/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder.request;

import java.util.function.Supplier;

import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryResult;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder;

/**
 * {@link TestDescriptor} for tests based on the JUnit Platform Suite API.
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

	private final SuiteLauncherDiscoveryRequestBuilder discoveryRequestBuilder = request();
	private final ConfigurationParameters configurationParameters;

	private LauncherDiscoveryResult launcherDiscoveryResult;
	private SuiteLauncher launcher;

	SuiteTestDescriptor(UniqueId id, Class<?> suiteClass, ConfigurationParameters configurationParameters) {
		super(requireNoCycles(id), getSuiteDisplayName(suiteClass), ClassSource.from(suiteClass));
		this.configurationParameters = configurationParameters;
	}

	private static UniqueId requireNoCycles(UniqueId id) {
		// @formatter:off
		boolean containsCycle = id.getSegments().stream()
				.filter(segment -> SuiteTestDescriptor.SEGMENT_TYPE.equals(segment.getType()))
				.map(Segment::getValue)
				.collect(groupingBy(identity(), counting()))
				.values()
				.stream()
				.anyMatch(count -> count > 1);
		// @formatter:on
		Supplier<String> message = () -> String.format(
			"Configuration error: The suite configuration may not contain a cycle [%s]", id);
		Preconditions.condition(!containsCycle, message);
		return id;
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
		return AnnotationUtils.findAnnotation(testClass, SuiteDisplayName.class)
				.map(SuiteDisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElse(testClass.getSimpleName());
		// @formatter:on
	}

	void execute(EngineExecutionListener listener) {
		listener.executionStarted(this);
		launcher.execute(launcherDiscoveryResult, listener);
		listener.executionFinished(this, TestExecutionResult.successful());
	}

}
