/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePreCreateCallback;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * Integration tests that verify support for {@link TestInstancePreCreateCallback}.
 *
 * @since 5.9.0
 */
class TestInstancePreCreateCallbackTests extends AbstractJupiterTestEngineTests {
	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	void instancePreCreate() {
		executeTestsForClass(OuterTestCase.class)
				.testEvents()
				.assertStatistics(stats -> stats.started(2).succeeded(2));

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
				"bazPreDestroyCallbackTestInstance:InnerTestCase",
				"barPreDestroyCallbackTestInstance:InnerTestCase",
				"fooPreDestroyCallbackTestInstance:InnerTestCase"
		);
		// @formatter:on
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
				"bazPreDestroyCallbackTestInstance:InnerTestCase",
				"barPreDestroyCallbackTestInstance:InnerTestCase",
			"fooPreDestroyCallbackTestInstance:InnerTestCase"
		);
		// @formatter:on
	}

	@Test @Disabled
	void testSpecificTestInstancePreDestroyCallbackIsCalled() {
		executeTestsForClass(TestCaseWithTestSpecificTestInstancePreDestroyCallback.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));

		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeEachMethod",
				"test",
			"fooPreDestroyCallbackTestInstance:TestCaseWithTestSpecificTestInstancePreDestroyCallback"
		);
		// @formatter:on
	}

	@Test @Disabled
	void classLifecyclePreDestroyCallbacks() {
		executeTestsForClass(PerClassLifecyclePreDestroyCallbacksWithTwoTestMethods.class).testEvents()//
				.assertStatistics(stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeEachMethod",
				"test1",
			"beforeEachMethod",
				"test2",
			"barPreDestroyCallbackTestInstance:PerClassLifecyclePreDestroyCallbacksWithTwoTestMethods",
			"fooPreDestroyCallbackTestInstance:PerClassLifecyclePreDestroyCallbacksWithTwoTestMethods"
		);
		// @formatter:on
	}

	// -------------------------------------------------------------------

	@ExtendWith(FooInstancePreCreateCallback.class)
	static class OuterTestCase {
		public OuterTestCase() {
			callSequence.add("constructor");
		}

		@BeforeEach
		void beforeOuterMethod() {
			callSequence.add("beforeOuterMethod");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@Nested
		@ExtendWith(BarInstancePreCreateCallback.class)
		@ExtendWith(BazInstancePreCreateCallback.class)
		class InnerTestCase {
			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}
		}
	}

	static class TestCaseWithTestSpecificTestInstancePreDestroyCallback {

		@BeforeEach
		void beforeEachMethod() {
			callSequence.add("beforeEachMethod");
		}

		@ExtendWith(FooInstancePreCreateCallback.class)
		@Test
		void test() {
			callSequence.add("test");
		}
	}

	@TestInstance(PER_CLASS)
	@ExtendWith(FooInstancePreCreateCallback.class)
	@ExtendWith(BarInstancePreCreateCallback.class)
	static class PerClassLifecyclePreDestroyCallbacksWithTwoTestMethods {

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

	static abstract class AbstractTestInstancePreCreateCallback implements TestInstancePreCreateCallback {
		private final String name;

		AbstractTestInstancePreCreateCallback(String name) {
			this.name = name;
		}

		@Override
		public void preCreateTestInstance(ExtensionContext context) {
			assertThat(context.getTestInstance()).isNotPresent();
			assertThat(context.getTestClass()).isPresent();
			callSequence.add(name + "PreCreateCallback:" + context.getTestClass().get().getSimpleName());
		}
	}

	static class FooInstancePreCreateCallback extends AbstractTestInstancePreCreateCallback {
		FooInstancePreCreateCallback() {
			super("foo");
		}
	}

	static class BarInstancePreCreateCallback extends AbstractTestInstancePreCreateCallback {
		BarInstancePreCreateCallback() {
			super("bar");
		}
	}

	static class BazInstancePreCreateCallback extends AbstractTestInstancePreCreateCallback {
		BazInstancePreCreateCallback() {
			super("baz");
		}
	}
}
