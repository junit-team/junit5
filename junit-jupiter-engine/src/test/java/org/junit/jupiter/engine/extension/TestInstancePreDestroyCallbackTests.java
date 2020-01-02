/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests that verify support for {@link TestInstancePreDestroyCallback}.
 *
 * @since 5.6
 */
class TestInstancePreDestroyCallbackTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	void instancePreDestroyCallbacksInNestedClasses() {
		executeTestsForClass(OuterTestCase.class).testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(

			// OuterTestCase
			"beforeOuterMethod",
				"testOuter",
			"fooPreDestroyCallbackTestInstance:OuterTestCase",

			// InnerTestCase
			"beforeOuterMethod",
				"beforeInnerMethod",
					"testInner",
				"barPreDestroyCallbackTestInstance:InnerTestCase",

			"fooPreDestroyCallbackTestInstance:InnerTestCase"
		);
		// @formatter:on
	}

	@Test
	void testSpecificTestInstancePreDestroyCallbackIsCalled() {
		executeTestsForClass(
			TestCaseWithTestSpecificTestInstancePreDestroyCallback.class).testEvents().assertStatistics(
				stats -> stats.started(1).succeeded(1));

		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeEachMethod",
				"test",
			"fooPreDestroyCallbackTestInstance:TestCaseWithTestSpecificTestInstancePreDestroyCallback"
		);
		// @formatter:on
	}

	@Test
	void classLifecyclePreDestroyCallbacks() {
		executeTestsForClass(PerClassLifecyclePreDestroyCallbackWithTwoTestMethods.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeEachMethod",
				"test1",
			"beforeEachMethod",
				"test2",
			"fooPreDestroyCallbackTestInstance:PerClassLifecyclePreDestroyCallbackWithTwoTestMethods"
		);
		// @formatter:on
	}

	// -------------------------------------------------------------------

	@ExtendWith(FooInstancePreDestroyCallback.class)
	static class OuterTestCase extends Destroyable {

		@BeforeEach
		void beforeOuterMethod() {
			callSequence.add("beforeOuterMethod");
		}

		@Test
		void testOuter() {
			assertFalse(isDestroyed);
			callSequence.add("testOuter");
		}

		@Nested
		@ExtendWith(BarInstancePreDestroyCallback.class)
		class InnerTestCase extends Destroyable {

			@BeforeEach
			void beforeInnerMethod() {
				assertFalse(isDestroyed);
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}
		}
	}

	static class TestCaseWithTestSpecificTestInstancePreDestroyCallback extends Destroyable {

		@BeforeEach
		void beforeEachMethod() {
			assertFalse(isDestroyed);
			callSequence.add("beforeEachMethod");
		}

		@ExtendWith(FooInstancePreDestroyCallback.class)
		@Test
		void test() {
			callSequence.add("test");
		}
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@ExtendWith(FooInstancePreDestroyCallback.class)
	static class PerClassLifecyclePreDestroyCallbackWithTwoTestMethods extends Destroyable {

		@BeforeEach
		void beforeEachMethod() {
			callSequence.add("beforeEachMethod");
		}

		@Test
		void test1() {
			callSequence.add("test1");
		}

		@Test
		void test2() {
			callSequence.add("test2");
		}
	}

	static class FooInstancePreDestroyCallback extends AbstractInstancePreDestroyCallback {

		protected FooInstancePreDestroyCallback() {
			super("foo");
		}
	}

	static class BarInstancePreDestroyCallback extends AbstractInstancePreDestroyCallback {

		protected BarInstancePreDestroyCallback() {
			super("bar");
		}
	}

	static abstract class AbstractInstancePreDestroyCallback implements TestInstancePreDestroyCallback {

		private final String name;

		AbstractInstancePreDestroyCallback(String name) {
			this.name = name;
		}

		@Override
		public void preDestroyTestInstance(ExtensionContext context) {
			assertTrue(context.getTestInstance().isPresent());
			Object testInstance = context.getTestInstance().get();
			if (testInstance instanceof Destroyable) {
				((Destroyable) testInstance).setDestroyed();
			}
			callSequence.add(name + "PreDestroyCallbackTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private abstract static class Destroyable {

		boolean isDestroyed;

		void setDestroyed() {
			isDestroyed = true;
		}
	}
}
