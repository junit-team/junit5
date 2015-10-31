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

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentestalliance.TestSkippedException;

/**
 * Unit tests for {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class JUnit5TestEngineTests {

	private JUnit5TestEngine engine;

	@org.junit.Before
	public void init() {
		engine = new JUnit5TestEngine();
	}

	@org.junit.Test
	public void executeTestsFromFromClasses() {

		TestPlanSpecification spec = TestPlanSpecification.build(
			TestPlanSpecification.forClassName(LocalTestCase.class.getName()));

		List<TestDescriptor> descriptors = discoverTests(spec);

		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", 8, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 6, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());
	}

	private List<TestDescriptor> discoverTests(TestPlanSpecification spec) {
		//For some reason the JUnit5Engine only works correctly if the engine descriptor is in the list of descriptors
		EngineDescriptor engineDescriptor = new EngineDescriptor(engine);
		List<TestDescriptor> descriptors = engine.discoverTests(spec, engineDescriptor);
		descriptors.add(engineDescriptor);
		return descriptors;
	}

	@org.junit.Test
	public void executeTestFromUniqueId() {
		TestPlanSpecification spec = TestPlanSpecification.build(TestPlanSpecification.forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"));

		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# tests", 3, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestFromUniqueIdWithExceptionThrownInAfterMethod() {
		TestPlanSpecification spec = TestPlanSpecification.build(TestPlanSpecification.forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#throwExceptionInAfterMethod()"));

		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# tests", 3, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeCompositeTestPlanSpecification() {
		TestPlanSpecification spec = TestPlanSpecification.build(
			TestPlanSpecification.forUniqueId(
				"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"),
			TestPlanSpecification.forClassName(LocalTestCase.class.getName()));

		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", 8, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 6, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());
	}

	private static class LocalTestCase {

		static boolean staticBeforeInvoked = false;

		boolean beforeInvoked = false;

		boolean throwExceptionInAfterMethod = false;

		@Before
		static void staticBefore() {
			staticBeforeInvoked = true;
		}

		@Before
		void before() {
			this.beforeInvoked = true;
			// Reset state, since the test instance is retained across all test methods;
			// otherwise, after() always throws an exception.
			this.throwExceptionInAfterMethod = false;
		}

		@After
		void after() {
			if (this.throwExceptionInAfterMethod) {
				throw new RuntimeException("Exception thrown from @After method");
			}
		}

		@Test
		void methodLevelCallbacks() {
			assertTrue(this.beforeInvoked, "@Before was not invoked on instance method");
			assertTrue(staticBeforeInvoked, "@Before was not invoked on static method");
		}

		@Test
		void throwExceptionInAfterMethod() {
			this.throwExceptionInAfterMethod = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@Test
		void skipped() {
			// TODO Switch to @Ignore once we support it.
			throw new TestSkippedException();
		}

		@Test
		void aborted() {
			assumeTrue(false);
		}

		@Test
		void alwaysFails() {
			fail("#fail");
		}

	}

	/**
	 * Simple {@link TestExecutionListener} that tracks the number of times
	 * that certain callbacks are invoked.
	 */
	private static class TrackingTestExecutionListener implements TestExecutionListener {

		final AtomicInteger testStartedCount = new AtomicInteger();
		final AtomicInteger testSucceededCount = new AtomicInteger();
		final AtomicInteger testSkippedCount = new AtomicInteger();
		final AtomicInteger testAbortedCount = new AtomicInteger();
		final AtomicInteger testFailedCount = new AtomicInteger();

		@Override
		public void testStarted(TestDescriptor testDescriptor) {
			testStartedCount.incrementAndGet();
		}

		@Override
		public void testSucceeded(TestDescriptor testDescriptor) {
			testSucceededCount.incrementAndGet();
		}

		@Override
		public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
			testSkippedCount.incrementAndGet();
		}

		@Override
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			testAbortedCount.incrementAndGet();
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			testFailedCount.incrementAndGet();
		}

	}

}
