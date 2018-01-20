/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExternalResource;
import org.junit.rules.Verifier;

class LauncherBasedEnableRuleMigrationSupportTests {

	@Test
	void enableRuleMigrationSupportAnnotationWorksForBothRuleTypes() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(
			EnableRuleMigrationSupportWithBothRuleTypesTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(true, EnableRuleMigrationSupportWithBothRuleTypesTestCase.afterOfRule1WasExecuted,
			"after of rule 1 executed?");
		assertEquals(true, EnableRuleMigrationSupportWithBothRuleTypesTestCase.beforeOfRule2WasExecuted,
			"before of rule 2 executed?");
		assertEquals(true, EnableRuleMigrationSupportWithBothRuleTypesTestCase.afterOfRule2WasExecuted,
			"before of rule 2 executed?");
	}

	@Test
	void verifierSupportForErrorCollectorFieldFailsTheTest() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(VerifierSupportForErrorCollectorTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(true, VerifierSupportForErrorCollectorTestCase.survivedBothErrors, "after of rule 1 executed?");
	}

	private final JupiterTestEngine engine = new JupiterTestEngine();

	private ExecutionEventRecorder executeTestsForClass(Class<?> testClass) {
		return executeTests(request().selectors(selectClass(testClass)).build());
	}

	private ExecutionEventRecorder executeTests(LauncherDiscoveryRequest request) {
		TestDescriptor testDescriptor = discoverTests(request);
		ExecutionEventRecorder eventRecorder = new ExecutionEventRecorder();
		engine.execute(new ExecutionRequest(testDescriptor, eventRecorder, request.getConfigurationParameters()));
		return eventRecorder;
	}

	private TestDescriptor discoverTests(LauncherDiscoveryRequest request) {
		return engine.discover(request, UniqueId.forEngine(engine.getId()));
	}

	@EnableRuleMigrationSupport
	static class EnableRuleMigrationSupportWithBothRuleTypesTestCase {

		static boolean afterOfRule1WasExecuted = false;

		static boolean beforeOfRule2WasExecuted = false;
		static boolean afterOfRule2WasExecuted = false;

		@Rule
		public Verifier verifier1 = new Verifier() {

			@Override
			protected void verify() throws Throwable {
				afterOfRule1WasExecuted = true;
			}
		};

		private ExternalResource resource2 = new ExternalResource() {
			@Override
			protected void before() throws Throwable {
				beforeOfRule2WasExecuted = true;
			}

			@Override
			protected void after() {
				afterOfRule2WasExecuted = true;
			}
		};

		@Rule
		public ExternalResource getResource2() {
			return resource2;
		}

		@Test
		void beforeMethodOfBothRule2WasExecuted() {
			assertTrue(beforeOfRule2WasExecuted);
		}

	}

	@ExtendWith(VerifierSupport.class)
	static class VerifierSupportForErrorCollectorTestCase {

		static boolean survivedBothErrors = false;

		@Rule
		public ErrorCollector collector = new ErrorCollector();

		@Test
		void addingTwoThrowablesToErrorCollectorFailsLate() {
			collector.addError(new Throwable("first thing went wrong"));
			collector.addError(new Throwable("second thing went wrong"));

			survivedBothErrors = true;
		}

	}

}
