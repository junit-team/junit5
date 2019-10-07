/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.nestedContainer;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.engine.EngineExecutionResults;

class NestedContainerEventConditionTests extends AbstractJupiterTestEngineTests {

	@Test
	void eventConditionsAssertionOnNestedClasses() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(ATestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		// @formatter:off
		executionResults.allEvents().assertEventsMatchExactly(
				event(engine(), started()),
					event(container(ATestCase.class), started()),
						event(test("test_a"), started()),
						event(test("test_a"), finishedSuccessfully()),
						event(nestedContainer(ATestCase.BTestCase.class), started()),
							event(test("test_b"), started()),
							event(test("test_b"), finishedSuccessfully()),
							event(nestedContainer(ATestCase.BTestCase.CTestCase.class), started()),
								event(test("test_c"), started()),
								event(test("test_c"), finishedSuccessfully()),
							event(nestedContainer(ATestCase.BTestCase.CTestCase.class), finishedSuccessfully()),
						event(nestedContainer(ATestCase.BTestCase.class), finishedSuccessfully()),
					event(container(ATestCase.class), finishedSuccessfully()),
				event(engine(), finishedSuccessfully()));
		// @formatter:on
	}

	@Test
	void exceptionsWhenUsingNestedContainerIncorrectly() {
		Assertions.assertDoesNotThrow(() -> container(ATestCase.class));
		Assertions.assertDoesNotThrow(() -> nestedContainer(ATestCase.class));

		Assertions.assertDoesNotThrow(() -> container(ATestCase.BTestCase.class));
		Assertions.assertDoesNotThrow(() -> nestedContainer(ATestCase.BTestCase.class));

		Assertions.assertDoesNotThrow(() -> container(ATestCase.BTestCase.CTestCase.class));
		Assertions.assertDoesNotThrow(() -> nestedContainer(ATestCase.BTestCase.CTestCase.class));

		Assertions.assertDoesNotThrow(() -> container(NestedContainerEventConditionTests.class));
		Assertions.assertThrows(AssertionError.class, () -> nestedContainer(NestedContainerEventConditionTests.class));
	}

	static class ATestCase {

		@Test
		void test_a() {
		}

		@Nested
		class BTestCase {
			@Test
			void test_b() {
			}

			@Nested
			class CTestCase {
				@Test
				void test_c() {
				}
			}
		}
	}
}
