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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Maps test execution events to engine execution events.
 *
 * This allows the {@link TestIdentifier}s in a {@link TestPlan} to be
 * represented as {@link TestDescriptor}s in an {@link ExecutionRequest}.
 *
 * @see TestIdentifierAsTestDescriptor
 */
final class EngineExecutionListenerAdaptor implements TestExecutionListener {

	private final Map<String, TestDescriptor> dynamicTests = new HashMap<>();
	private final SuiteTestDescriptor suiteTestDescriptor;
	private final EngineExecutionListener delegate;

	public EngineExecutionListenerAdaptor(SuiteTestDescriptor suiteTestDescriptor, EngineExecutionListener delegate) {
		this.suiteTestDescriptor = suiteTestDescriptor;
		this.delegate = delegate;
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		UniqueId uniqueId = suiteTestDescriptor.uniqueIdInSuite(testIdentifier);
		TestIdentifierAsTestDescriptor mappedTest = new TestIdentifierAsTestDescriptor(uniqueId, testIdentifier);
		dynamicTests.put(testIdentifier.getUniqueId(), mappedTest);
		delegate.dynamicTestRegistered(mappedTest);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		TestDescriptor mappedTest = findTestDescriptor(testIdentifier);
		delegate.executionSkipped(mappedTest, reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		TestDescriptor mappedTest = findTestDescriptor(testIdentifier);
		delegate.executionStarted(mappedTest);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		TestDescriptor mappedTest = findTestDescriptor(testIdentifier);
		delegate.executionFinished(mappedTest, testExecutionResult);
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		TestDescriptor mappedTest = findTestDescriptor(testIdentifier);
		delegate.reportingEntryPublished(mappedTest, entry);
	}

	private TestDescriptor findTestDescriptor(TestIdentifier testIdentifier) {
		UniqueId suiteTestId = suiteTestDescriptor.uniqueIdInSuite(testIdentifier);
		// @formatter:off
		return suiteTestDescriptor.getDescendants().stream()
				.map(TestDescriptor.class::cast)
				.filter(suiteTestDescriptor -> suiteTestId.equals(suiteTestDescriptor.getUniqueId()))
				.findFirst()
				.orElseGet(findDynamicTestDescriptor(testIdentifier));
		// @formatter:on
	}

	private Supplier<TestDescriptor> findDynamicTestDescriptor(TestIdentifier testIdentifier) {
		return () -> dynamicTests.get(testIdentifier.getUniqueId());
	}

}
