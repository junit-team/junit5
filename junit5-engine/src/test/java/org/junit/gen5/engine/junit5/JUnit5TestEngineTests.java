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
import static org.junit.gen5.api.Assumptions.*;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.Assert;
import org.junit.gen5.api.After;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentestalliance.TestSkippedException;

/**
 * Tests for {@link JUnit5TestEngine}.
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

	public void executeCompositeTestPlanSpecification() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"),
			forClassName(LocalTestCase.class.getName()));

		TrackingTestExecutionListener listener = executeTests(spec, 10);

		Assert.assertEquals("# tests started", 8, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 4, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsForClassName() {
		TestPlanSpecification spec = build(forClassName(LocalTestCase.class.getName()));

		TrackingTestExecutionListener listener = executeTests(spec, 10);

		Assert.assertEquals("# tests started", 8, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 4, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 1, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsWithDisabledTestClass() {
		TestPlanSpecification spec = build(forClassName(DisabledTestClassTestCase.class.getName()));

		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", 3, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 0, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsWithDisabledTestMethod() {
		TestPlanSpecification spec = build(forClassName(DisabledTestMethodTestCase.class.getName()));

		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", 4, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 1, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestForUniqueId() {
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#alwaysPasses()"));

		TrackingTestExecutionListener listener = executeTests(spec, 3);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestForUniqueIdWithExceptionThrownInAfterMethod() {
		TestPlanSpecification spec = build(forUniqueId(
			"junit5:org.junit.gen5.engine.junit5.JUnit5TestEngineTests$LocalTestCase#throwExceptionInAfterMethod()"));

		TrackingTestExecutionListener listener = executeTests(spec, 3);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());
	}

	private TrackingTestExecutionListener executeTests(TestPlanSpecification spec, int expectedDescriptorCount) {
		List<TestDescriptor> descriptors = discoverTests(spec);
		Assert.assertNotNull(descriptors);
		Assert.assertEquals("# descriptors", expectedDescriptorCount, descriptors.size());

		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();

		System.out.println("Descriptors: " + descriptors);
		engine.execute(new EngineExecutionContext(descriptors, listener));

		Assert.assertTrue("@BeforeAll was not invoked", LocalTestCase.beforeAllInvoked);
		Assert.assertTrue("@AfterAll was not invoked", LocalTestCase.afterAllInvoked);

		return listener;
	}

	private List<TestDescriptor> discoverTests(TestPlanSpecification spec) {
		// For some reason the JUnit5Engine only works correctly if the engine descriptor
		// is in the list of descriptors
		EngineDescriptor engineDescriptor = new EngineDescriptor(engine);
		List<TestDescriptor> descriptors = engine.discoverTests(spec, engineDescriptor);
		descriptors.add(engineDescriptor);
		return descriptors;
	}

	private static abstract class AbstractTestCase {

		@Test
		void fromSuperclass() {
			/* no-op */
		}

	}

	private static class LocalTestCase extends AbstractTestCase {

		static boolean beforeAllInvoked = false;

		static boolean afterAllInvoked = false;

		static boolean staticBeforeInvoked = false;

		boolean beforeInvoked = false;

		boolean throwExceptionInAfterMethod = false;

		@BeforeAll
		void beforeAll() {
			beforeAllInvoked = true;
		}

		@AfterAll
		void afterAll() {
			afterAllInvoked = true;
		}

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

		@CustomTestAnnotation
		void customTestAnnotation() {
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

	@Test
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
