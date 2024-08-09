/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests that verify support for {@link TestInstancePostProcessor}.
 *
 * @since 5.0
 */
class TestInstancePostProcessorTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	void instancePostProcessorsInNestedClasses() {
		executeTestsForClass(OuterTestCase.class).testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(

			// OuterTestCase
			"fooPostProcessTestInstance:OuterTestCase",
				"beforeOuterMethod",
					"testOuter",

			// InnerTestCase

			"fooPostProcessTestInstance:OuterTestCase",
			"fooPostProcessTestInstance:InnerTestCase",
				"barPostProcessTestInstance:InnerTestCase",
					"beforeOuterMethod",
						"beforeInnerMethod",
							"testInner"
		);
		// @formatter:on
	}

	@Test
	void testSpecificTestInstancePostProcessorIsCalled() {
		executeTestsForClass(TestCaseWithTestSpecificTestInstancePostProcessor.class).testEvents()//
				.assertStatistics(stats -> stats.started(1).succeeded(1));

		assertThat(callSequence).containsExactly(
			"fooPostProcessTestInstance:TestCaseWithTestSpecificTestInstancePostProcessor", "beforeEachMethod", "test");
	}

	// -------------------------------------------------------------------

	@ExtendWith(FooInstancePostProcessor.class)
	static class OuterTestCase implements Named {

		private String outerName;

		@Override
		public void setName(String name) {
			this.outerName = name;
		}

		@BeforeEach
		void beforeOuterMethod() {
			callSequence.add("beforeOuterMethod");
		}

		@Test
		void testOuter() {
			assertEquals("foo:" + OuterTestCase.class.getSimpleName(), outerName);
			callSequence.add("testOuter");
		}

		@Nested
		@ExtendWith(BarInstancePostProcessor.class)
		class InnerTestCase implements Named {

			private String innerName;

			@Override
			public void setName(String name) {
				this.innerName = name;
			}

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				assertEquals("foo:" + OuterTestCase.class.getSimpleName(), outerName);
				assertEquals("bar:" + InnerTestCase.class.getSimpleName(), innerName);
				callSequence.add("testInner");
			}
		}

	}

	static class TestCaseWithTestSpecificTestInstancePostProcessor implements Named {

		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@BeforeEach
		void beforeEachMethod() {
			callSequence.add("beforeEachMethod");
		}

		@ExtendWith(FooInstancePostProcessor.class)
		@Test
		void test() {
			callSequence.add("test");
			assertEquals("foo:" + getClass().getSimpleName(), name);
		}
	}

	static class FooInstancePostProcessor implements TestInstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("foo:" + context.getRequiredTestClass().getSimpleName());
			}
			callSequence.add("fooPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	static class BarInstancePostProcessor implements TestInstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("bar:" + context.getRequiredTestClass().getSimpleName());
			}
			callSequence.add("barPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private interface Named {

		void setName(String name);
	}

}
