/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static java.util.Arrays.asList;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.junit5.AbstractJUnit5TestEngineTests;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link InstancePostProcessor}.
 */
public class InstancePostProcessorTests extends AbstractJUnit5TestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@Test
	public void instancePostProcessorsInNestedClasses() {
		TestDiscoveryRequest request = request().select(forClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertEquals(asList(

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

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------

	@ExtendWith(FooInstancePostProcessor.class)
	private static class OuterTestCase implements Named {

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
			assertEquals("foo", outerName);
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
				assertEquals("foo", outerName);
				assertEquals("bar", innerName);
				callSequence.add("testInner");
			}
		}

	}

	private static class FooInstancePostProcessor implements InstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("foo");
			}
			callSequence.add("fooPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private static class BarInstancePostProcessor implements InstancePostProcessor {

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
			if (testInstance instanceof Named) {
				((Named) testInstance).setName("bar");
			}
			callSequence.add("barPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
		}
	}

	private interface Named {

		void setName(String name);
	}

}
