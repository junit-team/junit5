/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static java.util.Arrays.asList;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;

import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.*;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.ExtensionRegistry;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@link BeforeEach}, {@link AfterEach}, {@link BeforeEachExtensionPoint},
 * and {@link AfterEachExtensionPoint} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class MethodLevelCallbackTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void beforeEachAndAfterEachCallbacks() {
		TestPlanSpecification spec = testPlanSpecification().withElements(forClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(spec);

		assertEquals(2L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0L, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0L, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0L, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off

		assertEquals(asList(

			//InnerTestCase
			"outermostBefore",
				"fooBefore",
				"barBefore",
					"beforeMethod",
						"fizzBefore",
							"beforeInnerMethod",
								"innermostBefore",
									"testInner",
								"innermostAfter",
							"afterInnerMethod",
						"fizzAfter",
					"afterMethod",
				"barAfter",
				"fooAfter",
			"outermostAfter",

			//OuterTestCase
			"outermostBefore",
				"fooBefore",
				"barBefore",
					"beforeMethod",
						"innermostBefore",
							"testOuter",
						"innermostAfter",
					"afterMethod",
				"barAfter",
				"fooAfter",
			"outermostAfter"

			), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------

	private static List<String> callSequence = new ArrayList<>();

	@ExtendWith({ FooMethodLevelCallbacks.class, BarMethodLevelCallbacks.class, InnermostAndOutermost.class })
	private static class OuterTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}

		@Nested
		@ExtendWith(FizzMethodLevelCallbacks.class)
		class InnerTestCase {
			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}

			@AfterEach
			void afterInnerMethod() {
				callSequence.add("afterInnerMethod");
			}
		}

	}

	private static class InnermostAndOutermost implements ExtensionRegistrar {

		@Override
		public void registerExtensions(ExtensionRegistry registry) {
			registry.register(this::innermostBefore, BeforeEachExtensionPoint.class, Position.INNERMOST);
			registry.register(this::innermostAfter, AfterEachExtensionPoint.class, Position.INNERMOST);
			registry.register(this::outermostBefore, BeforeEachExtensionPoint.class, Position.OUTERMOST);
			registry.register(this::outermostAfter, AfterEachExtensionPoint.class, Position.OUTERMOST);
		}

		private void outermostBefore(TestExtensionContext context) {
			callSequence.add("outermostBefore");
		}

		private void innermostBefore(TestExtensionContext context) {
			callSequence.add("innermostBefore");
		}

		private void outermostAfter(TestExtensionContext context) {
			callSequence.add("outermostAfter");
		}

		private void innermostAfter(TestExtensionContext context) {
			callSequence.add("innermostAfter");
		}
	}

	private static class FooMethodLevelCallbacks implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("fooBefore");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("fooAfter");
		}

	}

	private static class BarMethodLevelCallbacks implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("barBefore");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("barAfter");
		}

	}

	private static class FizzMethodLevelCallbacks implements BeforeEachExtensionPoint, AfterEachExtensionPoint {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("fizzBefore");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("fizzAfter");
		}

	}
}
