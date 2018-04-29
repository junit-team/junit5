/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.execution.injection.sample.CustomAnnotation;
import org.junit.jupiter.engine.execution.injection.sample.CustomAnnotationParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.CustomType;
import org.junit.jupiter.engine.execution.injection.sample.CustomTypeParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.MapOfStringsParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.NullIntegerParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.NumberParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.PrimitiveArrayParameterResolver;
import org.junit.jupiter.engine.execution.injection.sample.PrimitiveIntegerParameterResolver;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Integration tests that verify support for {@link ParameterResolver}
 * extensions in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class ParameterResolverTests extends AbstractJupiterTestEngineTests {

	@Test
	void constructorInjection() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ConstructorInjectionTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void constructorInjectionWithAnnotatedParameter() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(
			AnnotatedParameterConstructorInjectionTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MethodInjectionTestCase.class);

		assertEquals(7, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(6, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForNullValuedMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(NullMethodInjectionTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		Predicate<String> expectations = s ->
				s.contains("NullIntegerParameterResolver") &&
				s.contains("resolved a null value for parameter") &&
				s.contains("but a primitive of type [int] is required");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(
				test("injectPrimitive"),
				finishedWithFailure(allOf(isA(ParameterResolutionException.class), message(expectations)))
			));
		// @formatter:on
	}

	@Test
	void executeTestsForPrimitiveIntegerMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PrimitiveIntegerMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForPrimitiveArrayMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PrimitiveArrayMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForPotentiallyIncompatibleTypeMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(
			PotentiallyIncompatibleTypeMethodInjectionTestCase.class);

		assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		Predicate<String> expectations = s ->
				s.contains("NumberParameterResolver") &&
				s.contains("resolved a value of type [java.lang.Integer]") &&
				s.contains("but a value assignment compatible with [java.lang.Double] is required");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(
				test("doubleParameterInjection"),
				finishedWithFailure(allOf(isA(ParameterResolutionException.class), message(expectations)
			))));
		// @formatter:on
	}

	@Test
	void executeTestsForMethodInjectionInBeforeAndAfterEachMethods() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(BeforeAndAfterMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodInjectionInBeforeAndAfterAllMethods() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(BeforeAndAfterAllMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodWithExtendWithAnnotation() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ExtendWithOnMethodTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForParameterizedTypesSelectingByClass() {
		assertEventsForParameterizedTypes(executeTestsForClass(ParameterizedTypeTestCase.class));
	}

	@Test
	void executeTestsForParameterizedTypesSelectingByFullyQualifiedMethodName() {
		String fqmn = ReflectionUtils.getFullyQualifiedMethodName(ParameterizedTypeTestCase.class, "testMapOfStrings",
			Map.class);

		assertEventsForParameterizedTypes(executeTests(request().selectors(selectMethod(fqmn)).build()));
	}

	@Disabled("Disabled until a decision has been made regarding #956")
	@Test
	void executeTestsForParameterizedTypesSelectingByFullyQualifiedMethodNameContainingGenericInfo() throws Exception {
		Method method = ParameterizedTypeTestCase.class.getDeclaredMethod("testMapOfStrings", Map.class);
		String genericParameterTypeName = method.getGenericParameterTypes()[0].getTypeName();
		String fqmn = String.format("%s#%s(%s)", ParameterizedTypeTestCase.class.getName(), "testMapOfStrings",
			genericParameterTypeName);

		assertEventsForParameterizedTypes(executeTests(request().selectors(selectMethod(fqmn)).build()));
	}

	private void assertEventsForParameterizedTypes(ExecutionEventRecorder eventRecorder) {
		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	// -------------------------------------------------------------------

	@ExtendWith(CustomTypeParameterResolver.class)
	static class ConstructorInjectionTestCase {

		private final TestInfo outerTestInfo;
		private final CustomType outerCustomType;

		ConstructorInjectionTestCase(TestInfo testInfo, CustomType customType) {
			this.outerTestInfo = testInfo;
			this.outerCustomType = customType;
		}

		@Test
		void test() {
			assertNotNull(this.outerTestInfo);
			assertNotNull(this.outerCustomType);
		}

		@Nested
		class NestedTestCase {

			private final TestInfo innerTestInfo;
			private final CustomType innerCustomType;

			NestedTestCase(TestInfo testInfo, CustomType customType) {
				this.innerTestInfo = testInfo;
				this.innerCustomType = customType;
			}

			@Test
			void test() {
				assertNotNull(outerTestInfo);
				assertNotNull(outerCustomType);
				assertNotNull(this.innerTestInfo);
				assertNotNull(this.innerCustomType);
			}
		}
	}

	@ExtendWith(CustomAnnotationParameterResolver.class)
	static class AnnotatedParameterConstructorInjectionTestCase {

		private final TestInfo outerTestInfo;
		private final CustomType outerCustomType;

		AnnotatedParameterConstructorInjectionTestCase(TestInfo testInfo, @CustomAnnotation CustomType customType) {
			this.outerTestInfo = testInfo;
			this.outerCustomType = customType;
		}

		@Test
		void test() {
			assertNotNull(this.outerTestInfo);
			assertNotNull(this.outerCustomType);
		}

		@Nested
		// See https://github.com/junit-team/junit5/issues/1345
		class AnnotatedConstructorParameterNestedTestCase {

			private final TestInfo innerTestInfo;
			private final CustomType innerCustomType;

			AnnotatedConstructorParameterNestedTestCase(TestInfo testInfo, @CustomAnnotation CustomType customType) {
				this.innerTestInfo = testInfo;
				this.innerCustomType = customType;
			}

			@Test
			void test() {
				assertNotNull(outerTestInfo);
				assertNotNull(outerCustomType);
				assertNotNull(this.innerTestInfo);
				assertNotNull(this.innerCustomType);
			}
		}
	}

	@ExtendWith({ CustomTypeParameterResolver.class, CustomAnnotationParameterResolver.class })
	static class MethodInjectionTestCase {

		@Test
		void parameterInjectionOfTestInfo(TestInfo testInfo) {
			assertNotNull(testInfo);
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

	@ExtendWith(NullIntegerParameterResolver.class)
	static class NullMethodInjectionTestCase {

		@Test
		void injectWrapper(Integer number) {
			assertNull(number);
		}

		@Test
		void injectPrimitive(int number) {
			// should never be invoked since an int cannot be null
		}
	}

	@ExtendWith(PrimitiveIntegerParameterResolver.class)
	static class PrimitiveIntegerMethodInjectionTestCase {

		@Test
		void intPrimitive(int i) {
			assertEquals(42, i);
		}
	}

	@ExtendWith(PrimitiveArrayParameterResolver.class)
	static class PrimitiveArrayMethodInjectionTestCase {

		@Test
		void primitiveArray(int... ints) {
			assertArrayEquals(new int[] { 1, 2, 3 }, ints);
		}
	}

	@ExtendWith(NumberParameterResolver.class)
	static class PotentiallyIncompatibleTypeMethodInjectionTestCase {

		@Test
		void numberParameterInjection(Number number) {
			assertEquals(Integer.valueOf(42), number);
		}

		@Test
		void integerParameterInjection(Integer number) {
			assertEquals(Integer.valueOf(42), number);
		}

		/**
		 * This test must fail, since {@link Double} is a {@link Number} but not an {@link Integer}.
		 * @see NumberParameterResolver
		 */
		@Test
		void doubleParameterInjection(Double number) {
			/* no-op */
		}
	}

	static class BeforeAndAfterMethodInjectionTestCase {

		@BeforeEach
		void before(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}

		@Test
		@DisplayName("custom name")
		void customNamedTest() {
		}

		@AfterEach
		void after(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}
	}

	@DisplayName("custom class name")
	static class BeforeAndAfterAllMethodInjectionTestCase {

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}

		@Test
		void aTest() {
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}
	}

	static class ExtendWithOnMethodTestCase {

		/**
		 * This set-up / tear-down method is here to verify that {@code @BeforeEach}
		 * and {@code @AfterEach} methods are properly invoked using the same
		 * {@code ExtensionRegistry} as the one used for the corresponding
		 * {@code @Test} method.
		 *
		 * @see <a href="https://github.com/junit-team/junit5/issues/523">#523</a>
		 */
		@BeforeEach
		@AfterEach
		void setUpAndTearDown(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}

		@Test
		@ExtendWith(CustomTypeParameterResolver.class)
		@ExtendWith(CustomAnnotationParameterResolver.class)
		void testMethodWithExtensionAnnotation(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

	static class ParameterizedTypeTestCase {

		@Test
		@ExtendWith(MapOfStringsParameterResolver.class)
		void testMapOfStrings(Map<String, String> map) {
			assertNotNull(map);
			assertEquals("value", map.get("key"));
		}
	}

}
