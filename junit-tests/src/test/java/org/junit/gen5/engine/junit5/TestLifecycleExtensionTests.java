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

import static java.util.Arrays.asList;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.api.extension.TestLifecycleExtension;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@link BeforeEach}, {@link AfterEach},
 * {@link org.junit.gen5.api.extension.TestLifecycleExtension}in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
@Ignore("https://github.com/junit-team/junit-lambda/issues/39")

public class TestLifecycleExtensionTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void beforeEachAndAfterEachCallbacksWithTestInstancePerMethod() {
		TestPlanSpecification spec = build(forClass(TestLifecycleTestCase.class));

		TrackingTestExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertTrue("@BeforeEach was not invoked", TestLifecycleTestCase.beforeEachInvoked);
		Assert.assertTrue("@AfterEach was not invoked", TestLifecycleTestCase.afterEachInvoked);

		Assert.assertEquals("preBeforeEach()", asList("foo", "bar"), preBeforeEachMethods);
		Assert.assertEquals("postAfterEach()", asList("bar", "foo"), postAfterEachMethods);
	}

	// -------------------------------------------------------------------

	@ExtendWith(value = { FooTestLifecycleExtension.class,
			BarTestLifecycleExtension.class }, order = TestExtension.OrderPosition.INNERMOST)
	private static class TestLifecycleTestCase {

		static boolean beforeEachInvoked = false;

		static boolean afterEachInvoked = false;

		@BeforeEach
		void beforeEach() {
			beforeEachInvoked = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@AfterEach
		void afterEach() {
			afterEachInvoked = true;
		}

	}

	private static List<String> preBeforeEachMethods = new ArrayList<>();
	private static List<String> postAfterEachMethods = new ArrayList<>();

	private static class FooTestLifecycleExtension implements TestLifecycleExtension {

		@Override
		public void beforeEach(TestExtensionContext methodExecutionContext) {
			preBeforeEachMethods.add("foo");
		}

		@Override
		public void afterEach(TestExtensionContext methodExecutionContext) {
			postAfterEachMethods.add("foo");
		}

	}

	private static class BarTestLifecycleExtension implements TestLifecycleExtension {

		@Override
		public void beforeEach(TestExtensionContext testExtensionContext) {
			preBeforeEachMethods.add("bar");
		}

		@Override
		public void afterEach(TestExtensionContext testExtensionContext) {
			postAfterEachMethods.add("bar");
		}

	}

}
