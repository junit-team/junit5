/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.gen5.engine.test.event.ExecutionEventRecorder;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that verify support for {@link Disabled @Disabled} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class DisabledTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void executeTestsWithDisabledTestClass() {
		TestDiscoveryRequest request = request().selectors(selectClass(DisabledTestClassTestCase.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, eventRecorder.getTestStartedCount(), "# tests started");
	}

	@Test
	public void executeTestsWithDisabledTestMethods() {
		TestDiscoveryRequest request = request().selectors(selectClass(DisabledTestMethodsTestCase.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestSkippedCount(), "# tests skipped");
	}

	// -------------------------------------------------------------------

	@Disabled
	private static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	private static class DisabledTestMethodsTestCase {

		@Test
		void enabledTest() {
		}

		@Test
		@Disabled
		void disabledTest() {
			fail("this should be @Disabled");
		}

	}

}
