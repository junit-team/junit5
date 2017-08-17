/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

class JUnit4ParameterizedTests {
	private final Launcher launcher = LauncherFactory.create();
	private final Map<TestExecutionResult.Status, Integer> callCounts = new HashMap<>();
	private final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();

	@BeforeEach
	void setUpLauncher() {
		launcher.registerTestExecutionListeners(new TestListenerMock());
	}

	@Test
	void selectingWholeParameterizedClassRunsTestsWithAllValues() {
		requestBuilder.selectors(selectClass(JUnit4ParameterizedTestCase.class));
		LauncherDiscoveryRequest discoveryRequest = requestBuilder.build();

		launcher.execute(discoveryRequest);

		HashMap<TestExecutionResult.Status, Integer> expectedCallCounts = new HashMap<>();
		expectedCallCounts.put(TestExecutionResult.Status.SUCCESSFUL, 3);
		expectedCallCounts.put(TestExecutionResult.Status.FAILED, 9);
		assertEquals(expectedCallCounts, callCounts);
	}

	@Test
	void selectingOneTestFromParameterizedClassRunsWithAllValues() {
		requestBuilder.selectors(selectMethod(JUnit4ParameterizedTestCase.class, "test1"));
		LauncherDiscoveryRequest discoveryRequest = requestBuilder.build();

		launcher.execute(discoveryRequest);

		HashMap<TestExecutionResult.Status, Integer> expectedCallCounts = new HashMap<>();
		expectedCallCounts.put(TestExecutionResult.Status.FAILED, 3);
		assertEquals(expectedCallCounts, callCounts);
	}

	private class TestListenerMock implements TestExecutionListener {
		@Override
		public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {
			if (identifier.isTest()) {
				final TestExecutionResult.Status status = result.getStatus();
				callCounts.merge(status, 1, Integer::sum);
			}
		}
	}
}
