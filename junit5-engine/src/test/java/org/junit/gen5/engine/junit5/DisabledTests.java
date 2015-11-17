/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Assert;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@link Disabled} in the {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class DisabledTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void executeTestsWithDisabledTestClass() {
		TestPlanSpecification spec = build(forClass(DisabledTestClassTestCase.class));
		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 0, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsWithDisabledTestMethod() {
		TestPlanSpecification spec = build(forClass(DisabledTestMethodTestCase.class));
		TrackingTestExecutionListener listener = executeTests(spec, 3);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	// -------------------------------------------------------------------

	@Disabled
	private static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
		}
	}

	private static class DisabledTestMethodTestCase {

		@Test
		void enabledTest() {
		}

		@Test
		@Disabled
		void disabledTest() {
		}
	}

}
