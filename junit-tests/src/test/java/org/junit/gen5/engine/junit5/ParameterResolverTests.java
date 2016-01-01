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

import static org.junit.gen5.api.Assertions.*;

import org.junit.Assert;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestName;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotationParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomType;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomTypeParameterResolver;

/**
 * Integration tests that verify support for {@link MethodParameterResolver}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class ParameterResolverTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void executeTestsForMethodInjectionCases() {
		TrackingEngineExecutionListener listener = executeTestsForClass(MethodInjectionTestCase.class, 9);

		Assert.assertEquals("# tests started", 8, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 7, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsForMethodInjectionInBeforeAndAfterEachMethods() {
		TrackingEngineExecutionListener listener = executeTestsForClass(BeforeAndAfterMethodInjectionTestCase.class, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsForMethodInjectionInBeforeAndAfterAllMethods() {
		TrackingEngineExecutionListener listener = executeTestsForClass(BeforeAndAfterAllMethodInjectionTestCase.class,
			2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	@org.junit.Test
	public void executeTestsForMethodWithExtendWithAnnotation() {
		TrackingEngineExecutionListener listener = executeTestsForClass(ExtendWithOnMethodTestCase.class, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());
	}

	// -------------------------------------------------------------------

	@ExtendWith({ CustomTypeParameterResolver.class, CustomAnnotationParameterResolver.class })
	private static class MethodInjectionTestCase {

		@Test
		void parameterInjectionOfStandardTestName(@TestName String name) {
			assertEquals("parameterInjectionOfStandardTestName", name);
		}

		@Test
		@Name("myName")
		void parameterInjectionOfUserProvidedTestName(@TestName String name) {
			assertEquals("myName", name);
		}

		@Test
		void parameterInjectionWithCompetingResolversFail(@CustomAnnotation CustomType customType) {
			// should fail
		}

		@Test
		void parameterInjectionByType(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void parameterInjectionByAnnotation(@CustomAnnotation String value) {
			assertNotNull(value);
		}

		// some overloaded methods

		@Test
		void overloadedName() {
			assertTrue(true);
		}

		@Test
		void overloadedName(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void overloadedName(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

	private static class BeforeAndAfterMethodInjectionTestCase {

		@BeforeEach
		void before(@TestName String name) {
			assertEquals("custom name", name);
		}

		@Test
		@Name("custom name")
		void customNamedTest() {
		}

		@AfterEach
		void after(@TestName String name) {
			assertEquals("custom name", name);
		}
	}

	@Name("custom class name")
	private static class BeforeAndAfterAllMethodInjectionTestCase {

		@BeforeAll
		static void beforeAll(@TestName String name) {
			assertEquals("custom class name", name);
		}

		@Test
		void aTest() {
		}

		@AfterAll
		static void afterAll(@TestName String name) {
			assertEquals("custom class name", name);
		}
	}

	private static class ExtendWithOnMethodTestCase {

		@Test
		@ExtendWith(CustomTypeParameterResolver.class)
		@ExtendWith(CustomAnnotationParameterResolver.class)
		void testMethodWithExtensionAnnotation(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

}
