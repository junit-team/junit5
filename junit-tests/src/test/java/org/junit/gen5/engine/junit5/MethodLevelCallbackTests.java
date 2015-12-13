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
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@link BeforeEach}, {@link AfterEach},
 * {@link BeforeEachExtensionPoint}, and {@link AfterEachExtensionPoint} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class MethodLevelCallbackTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void beforeEachAndAfterEachCallbacksWithTestInstancePerMethod() {
		TestPlanSpecification spec = build(forClass(InstancePerMethodTestCase.class));

		TrackingEngineExecutionListener listener = executeTests(spec, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertEquals("wrong before each call sequence", asList("foo", "bar", "method"), beforeEachCalls);
		Assert.assertEquals("wrong after each call sequence", asList("method", "bar", "foo"), afterEachCalls);
	}

	// -------------------------------------------------------------------

	private static List<String> beforeEachCalls = new ArrayList<>();
	private static List<String> afterEachCalls = new ArrayList<>();

	@ExtendWith({ FooMethodLevelCallbacks.class, BarMethodLevelCallbacks.class })
	private static class InstancePerMethodTestCase {

		@BeforeEach
		void beforeEach() {
			beforeEachCalls.add("method");
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@AfterEach
		void afterEach() {
			afterEachCalls.add("method");
		}

	}

	private static class FooMethodLevelCallbacks implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		@Override
		public void beforeEach(TestExtensionContext context) {
			beforeEachCalls.add("foo");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			afterEachCalls.add("foo");
		}

	}

	private static class BarMethodLevelCallbacks implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		@Override
		public void beforeEach(TestExtensionContext context) {
			beforeEachCalls.add("bar");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			afterEachCalls.add("bar");
		}

	}

}
