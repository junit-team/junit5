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
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.AfterAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.ExtensionRegistry;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support of {@link BeforeAll}, {@link AfterAll},
 * and {@link BeforeAllExtensionPoint} in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class ClassLevelCallbackTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void beforeAllAndAfterAllCallbacks() {
		TestPlanSpecification spec = build(forClass(InstancePerMethodTestCase.class));

		ExecutionEventRecorder eventRecorder = executeTests(spec);

		assertEquals(1L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertEquals(asList(
			"outermostBefore",
				"fooBeforeAll",
				"barBeforeAll",
					"beforeAllMethod",
						"innermostBefore",
							"firstTest",
						"innermostAfter",
					"afterAllMethod",
				"barAfterAll",
				"fooAfterAll",
			"outermostAfter"
			), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------

	private static List<String> callSequence = new ArrayList<>();

	@ExtendWith({ FooClassLevelCallbacks.class, BarClassLevelCallbacks.class, InnermostAndOutermost.class })
	private static class InstancePerMethodTestCase {

		@BeforeAll
		static void beforeAll() {
			callSequence.add("beforeAllMethod");
		}

		@AfterAll
		static void afterAll() {
			callSequence.add("afterAllMethod");
		}

		@Test
		void firstTest() {
			callSequence.add("firstTest");
		}

	}

	private static class InnermostAndOutermost implements ExtensionRegistrar {

		@Override
		public void registerExtensions(ExtensionRegistry registry) {
			registry.register(this::innermostBefore, BeforeAllExtensionPoint.class, Position.INNERMOST);
			registry.register(this::innermostAfter, AfterAllExtensionPoint.class, Position.INNERMOST);
			registry.register(this::outermostBefore, BeforeAllExtensionPoint.class, Position.OUTERMOST);
			registry.register(this::outermostAfter, AfterAllExtensionPoint.class, Position.OUTERMOST);
		}

		private void outermostBefore(ContainerExtensionContext context) {
			callSequence.add("outermostBefore");
		}

		private void innermostBefore(ContainerExtensionContext context) {
			callSequence.add("innermostBefore");
		}

		private void outermostAfter(ContainerExtensionContext context) {
			callSequence.add("outermostAfter");
		}

		private void innermostAfter(ContainerExtensionContext context) {
			callSequence.add("innermostAfter");
		}
	}

	private static class FooClassLevelCallbacks implements BeforeAllExtensionPoint, AfterAllExtensionPoint {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooAfterAll");
		}
	}

	private static class BarClassLevelCallbacks implements BeforeAllExtensionPoint, AfterAllExtensionPoint {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barAfterAll");
		}
	}

}
