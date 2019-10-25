/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.nestedContainer;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 1.6
 */
class NestedContainerEventConditionTests {

	@Test
	void preconditions() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> nestedContainer(null))//
				.withMessage("Class must not be null");

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> nestedContainer(NestedContainerEventConditionTests.class))//
				.withMessage(NestedContainerEventConditionTests.class.getName() + " must be a nested class");
	}

	@Test
	void usingNestedContainerCorrectly() {
		assertDoesNotThrow(() -> container(ATestCase.class));
		assertDoesNotThrow(() -> nestedContainer(ATestCase.class));

		assertDoesNotThrow(() -> container(ATestCase.BTestCase.class));
		assertDoesNotThrow(() -> nestedContainer(ATestCase.BTestCase.class));

		assertDoesNotThrow(() -> container(ATestCase.BTestCase.CTestCase.class));
		assertDoesNotThrow(() -> nestedContainer(ATestCase.BTestCase.CTestCase.class));

		assertDoesNotThrow(() -> container(NestedContainerEventConditionTests.class));
	}

	@Test
	void eventConditionsForMultipleLevelsOfNestedClasses() {
		// @formatter:off
		EngineTestKit.engine("junit-jupiter")
			.selectors(selectClass(ATestCase.class))
			.execute()
			.allEvents()
			.assertEventsMatchExactly(
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
				event(engine(), finishedSuccessfully())
			);
		// @formatter:on
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
