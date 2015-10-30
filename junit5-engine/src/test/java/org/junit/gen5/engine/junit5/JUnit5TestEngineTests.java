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

import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Unit tests for {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class JUnit5TestEngineTests {

	private static final EngineDescriptor ENGINE_DESCRIPTOR = new EngineDescriptor("engine-id");

	@org.junit.Test
	public void executeTestsFromFromClasses() {
		JUnit5TestEngine engine = new JUnit5TestEngine();

		TestPlanSpecification spec = TestPlanSpecification.build(
			TestPlanSpecification.forClassName(LocalTestCase.class.getName()));

		List<TestDescriptor> descriptors = engine.discoverTests(spec, ENGINE_DESCRIPTOR);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors (class + methods)", 4, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals(3, listener.testStartedCount.get());
		Assert.assertEquals(1, listener.testSucceededCount.get());
		Assert.assertEquals(1, listener.testAbortedCount.get());
		Assert.assertEquals(1, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestFromUniqueId() {
		JUnit5TestEngine engine = new JUnit5TestEngine();

		TestPlanSpecification spec = TestPlanSpecification.build(TestPlanSpecification.forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"));

		List<TestDescriptor> descriptors = engine.discoverTests(spec, ENGINE_DESCRIPTOR);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# tests", 1, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals(1, listener.testStartedCount.get());
		Assert.assertEquals(1, listener.testSucceededCount.get());
	}

	@org.junit.Test
	public void executeCompositeTestPlanSpecification() {
		JUnit5TestEngine engine = new JUnit5TestEngine();

		TestPlanSpecification spec = TestPlanSpecification.build(
			TestPlanSpecification.forUniqueId(
				"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"),
			TestPlanSpecification.forClassName(LocalTestCase.class.getName()));

		List<TestDescriptor> descriptors = engine.discoverTests(spec, ENGINE_DESCRIPTOR);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", 4 + 1, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals(3 + 1, listener.testStartedCount.get());
		Assert.assertEquals(1 + 1, listener.testSucceededCount.get());
	}

	private static class LocalTestCase {

		@Test
		void alwaysPasses() {
			/* no-op */
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
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			testAbortedCount.incrementAndGet();
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			testFailedCount.incrementAndGet();
		}

	}

}
