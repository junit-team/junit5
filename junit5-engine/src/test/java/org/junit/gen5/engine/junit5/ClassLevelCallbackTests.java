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

import static org.junit.gen5.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Assert;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInstance;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support of {@link BeforeAll} and {@link AfterAll}
 * in the {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class ClassLevelCallbackTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void beforeAllAndAfterAllCallbacksWithTestInstancePerMethod() {
		TestPlanSpecification spec = build(forClass(InstancePerMethodTestCase.class));

		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertTrue("@BeforeAll was not invoked", InstancePerMethodTestCase.beforeAllInvoked);
		Assert.assertTrue("@AfterAll was not invoked", InstancePerMethodTestCase.afterAllInvoked);
	}

	@org.junit.Test
	public void beforeAllAndAfterAllCallbacksWithTestInstancePerClass() {
		TestPlanSpecification spec = build(forClass(InstancePerClassTestCase.class));

		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertTrue("@BeforeAll was not invoked", InstancePerClassTestCase.beforeAllInvoked);
		Assert.assertTrue("@AfterAll was not invoked", InstancePerClassTestCase.afterAllInvoked);
	}

	// -------------------------------------------------------------------

	private static class InstancePerMethodTestCase {

		static boolean beforeAllInvoked = false;

		static boolean afterAllInvoked = false;

		@BeforeAll
		// MUST be static for TestInstance.Lifecycle.PER_METHOD!
		static void beforeAll() {
			beforeAllInvoked = true;
		}

		@AfterAll
		// MUST be static for TestInstance.Lifecycle.PER_METHOD!
		static void afterAll() {
			afterAllInvoked = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}
	}

	@TestInstance(PER_CLASS)
	private static class InstancePerClassTestCase {

		static boolean beforeAllInvoked = false;

		static boolean afterAllInvoked = false;

		@BeforeAll
		void beforeAll() {
			beforeAllInvoked = true;
		}

		@AfterAll
		void afterAll() {
			afterAllInvoked = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}
	}

}
