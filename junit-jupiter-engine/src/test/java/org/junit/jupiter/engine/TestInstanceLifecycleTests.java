/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link TestInstance @TestInstance} lifecycle support.
 *
 * @since 5.0
 */
@RunWith(JUnitPlatform.class)
public

class TestInstanceLifecycleTests extends AbstractJupiterTestEngineTests {

	private static final Map<String, Object> instanceMap = new LinkedHashMap<>();

	private static int instanceCount;
	private static int nestedInstanceCount;
	private static int beforeAllCount;
	private static int afterAllCount;
	private static int beforeEachCount;
	private static int afterEachCount;
	private static String lastTestInvoked;

	@BeforeEach
	void init() {
		instanceMap.clear();
		instanceCount = 0;
		nestedInstanceCount = 0;
		beforeAllCount = 0;
		afterAllCount = 0;
		beforeEachCount = 0;
		afterEachCount = 0;
		lastTestInvoked = null;
	}

	@Test
	void instancePerMethod() {
		Class<?> testClass = InstancePerMethodTestCase.class;
		int containers = 2;
		int tests = 2;
		int instances = 2;
		int nestedInstances = 0;
		int allMethods = 1;
		int eachMethods = 2;

		performAssertions(testClass, containers, tests, instances, nestedInstances, allMethods, eachMethods);

		String fistTestInvoked = lastTestInvoked.equals("test2") ? "test1" : "test2";

		String containerExecutionConditionKey = containerExecutionConditionKey(testClass);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, fistTestInvoked);
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, fistTestInvoked);
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, lastTestInvoked);
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, lastTestInvoked);

		// @formatter:off
		// Check order here.
		assertThat(instanceMap.keySet()).containsExactly(
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				postProcessTestInstanceKey,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				afterAllCallbackKey
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		Object instance = instanceMap.get(beforeEachCallbackKey1);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey1));

		instance = instanceMap.get(beforeEachCallbackKey2);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey2));
		assertSame(instance, instanceMap.get(postProcessTestInstanceKey));
	}

	@Test
	void instancePerClass() {
		Class<?> testClass = InstancePerClassTestCase.class;
		int containers = 2;
		int tests = 2;
		int instances = 1;
		int nestedInstances = 0;
		int allMethods = 2;
		int eachMethods = 2;

		performAssertions(testClass, containers, tests, instances, nestedInstances, allMethods, eachMethods);

		String fistTestInvoked = lastTestInvoked.equals("test2") ? "test1" : "test2";

		String containerExecutionConditionKey = containerExecutionConditionKey(testClass);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, fistTestInvoked);
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, fistTestInvoked);
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, lastTestInvoked);
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, lastTestInvoked);

		// @formatter:off
		// Check order here.
		assertThat(instanceMap.keySet()).containsExactly(
				postProcessTestInstanceKey,
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				afterAllCallbackKey
		);
		// @formatter:on

		Object instance = instanceMap.get(beforeAllCallbackKey);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterAllCallbackKey));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey1));
		assertSame(instance, instanceMap.get(afterEachCallbackKey1));
		assertSame(instance, instanceMap.get(postProcessTestInstanceKey));
		assertSame(instance, instanceMap.get(containerExecutionConditionKey));
	}

	@Test
	void instancePerMethodWithNestedTestClass() {
		Class<?> testClass = InstancePerMethodOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerMethodOuterTestCase.NestedInstancePerMethodTestCase.class;
		int containers = 3;
		int tests = 3;
		int instances = 3;
		int nestedInstances = 2;
		int allMethods = 1;
		int eachMethods = 2;

		performAssertions(testClass, containers, tests, instances, nestedInstances, allMethods, eachMethods);

		String fistTestInvoked = lastTestInvoked.equals("test2") ? "test1" : "test2";

		String containerExecutionConditionKey = containerExecutionConditionKey(testClass);
		String nestedContainerExecutionConditionKey = containerExecutionConditionKey(nestedTestClass);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, lastTestInvoked);
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, lastTestInvoked);

		// @formatter:off
		// Don't check order here.
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertNull(instanceMap.get(nestedBeforeAllCallbackKey));
		assertNull(instanceMap.get(nestedAfterAllCallbackKey));

		Object instance = instanceMap.get(beforeEachCallbackKey);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey));

		Object nestedInstance1 = instanceMap.get(nestedBeforeEachCallbackKey1);
		assertNotNull(nestedInstance1);
		assertNotSame(instance, nestedInstance1);
		assertSame(nestedInstance1, instanceMap.get(nestedAfterEachCallbackKey1));

		Object nestedInstance2 = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance2);
		assertNotSame(instance, nestedInstance2);
		assertNotSame(nestedInstance1, nestedInstance2);
		assertSame(nestedInstance2, instanceMap.get(nestedBeforeEachCallbackKey2));
		assertSame(nestedInstance2, instanceMap.get(nestedAfterEachCallbackKey2));

		Object outerInstance1 = ReflectionUtils.getOuterInstance(nestedInstance1, testClass).get();
		Object outerInstance2 = ReflectionUtils.getOuterInstance(nestedInstance2, testClass).get();
		assertNotSame(outerInstance1, outerInstance2);

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class for
		// test2().
		assertSame(outerInstance2, instanceMap.get(postProcessTestInstanceKey));
	}

	@Test
	void instancePerClassWithNestedTestClass() {
		Class<?> testClass = InstancePerClassOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerClassOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 3;
		int tests = 3;
		int instances = 1;
		int nestedInstances = 1;
		int allMethods = 2;
		int eachMethods = 2;

		performAssertions(testClass, containers, tests, instances, nestedInstances, allMethods, eachMethods);

		String fistTestInvoked = lastTestInvoked.equals("test2") ? "test1" : "test2";

		String containerExecutionConditionKey = containerExecutionConditionKey(testClass);
		String nestedContainerExecutionConditionKey = containerExecutionConditionKey(nestedTestClass);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, lastTestInvoked);
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, lastTestInvoked);

		// @formatter:off
		// Don't check order here.
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2
		);
		// @formatter:on

		Object instance = instanceMap.get(postProcessTestInstanceKey);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(containerExecutionConditionKey));
		assertSame(instance, instanceMap.get(beforeAllCallbackKey));
		assertSame(instance, instanceMap.get(afterAllCallbackKey));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey));
		assertSame(instance, instanceMap.get(afterEachCallbackKey));

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance);
		assertNotSame(instance, nestedInstance);
		assertSame(nestedInstance, instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2));

		Object outerInstance = ReflectionUtils.getOuterInstance(nestedInstance, testClass).get();
		assertSame(outerInstance, instanceMap.get(postProcessTestInstanceKey));
	}

	@Test
	void instancePerMethodOnOuterTestClassWithInstancePerClassOnNestedTestClass() {
		Class<?> testClass = MixedLifecyclesOuterTestCase.class;
		Class<?> nestedTestClass = MixedLifecyclesOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 3;
		int tests = 3;
		int instances = 2;
		int nestedInstances = 1;
		int allMethods = 1;
		int eachMethods = 5;

		performAssertions(testClass, containers, tests, instances, nestedInstances, allMethods, eachMethods);

		String fistTestInvoked = lastTestInvoked.equals("test2") ? "test1" : "test2";

		String containerExecutionConditionKey = containerExecutionConditionKey(testClass);
		String nestedContainerExecutionConditionKey = containerExecutionConditionKey(nestedTestClass);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, fistTestInvoked);
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, lastTestInvoked);
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, lastTestInvoked);

		// @formatter:off
		// Don't check order here.
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		Object instance = instanceMap.get(beforeEachCallbackKey);
		assertSame(instance, instanceMap.get(afterEachCallbackKey));

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance);
		assertNotSame(instance, nestedInstance);
		assertSame(nestedInstance, instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2));

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class.
		Object outerInstance = ReflectionUtils.getOuterInstance(nestedInstance, testClass).get();
		assertEquals(instance.getClass(), outerInstance.getClass());
		assertNotSame(instance, outerInstance);
		assertSame(outerInstance, instanceMap.get(postProcessTestInstanceKey));
	}

	private void performAssertions(Class<?> testClass, int containers, int tests, int instances, int nestedInstances,
			int allMethods, int eachMethods) {

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		// eventRecorder.eventStream().forEach(System.out::println);

		// @formatter:off
		assertAll(
			() -> assertEquals(containers, eventRecorder.getContainerStartedCount(), "# containers started"),
			() -> assertEquals(containers, eventRecorder.getContainerFinishedCount(), "# containers finished"),
			() -> assertEquals(tests, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(tests, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(instances, instanceCount, "instance count"),
			() -> assertEquals(nestedInstances, nestedInstanceCount, "nested instance count"),
			() -> assertEquals(allMethods, beforeAllCount, "@BeforeAll count"),
			() -> assertEquals(allMethods, afterAllCount, "@AfterAll count"),
			() -> assertEquals(eachMethods, beforeEachCount, "@BeforeEach count"),
			() -> assertEquals(eachMethods, afterEachCount, "@AfterEach count")
		);
		// @formatter:on
	}

	private static String containerExecutionConditionKey(Class<?> testClass) {
		return "ContainerExecutionCondition." + testClass.getSimpleName();
	}

	private static String postProcessTestInstanceKey(Class<?> testClass) {
		return "TestInstancePostProcessor." + testClass.getSimpleName();
	}

	private static String beforeAllCallbackKey(Class<?> testClass) {
		return "BeforeAllCallback." + testClass.getSimpleName();
	}

	private static String afterAllCallbackKey(Class<?> testClass) {
		return "AfterAllCallback." + testClass.getSimpleName();
	}

	private static String beforeEachCallbackKey(Class<?> testClass, String testMethod) {
		return "BeforeEachCallback." + testClass.getSimpleName() + "." + testMethod;
	}

	private static String afterEachCallbackKey(Class<?> testClass, String testMethod) {
		return "AfterEachCallback." + testClass.getSimpleName() + "." + testMethod;
	}

	// -------------------------------------------------------------------------

	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class InstancePerMethodTestCase {

		InstancePerMethodTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void test1(TestInfo testInfo) {
			lastTestInvoked = testInfo.getTestMethod().get().getName();
		}

		@Test
		void test2(TestInfo testInfo) {
			lastTestInvoked = testInfo.getTestMethod().get().getName();
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@AfterAll
		static void afterAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	private static class InstancePerClassTestCase extends InstancePerMethodTestCase {

		@BeforeAll
		void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@AfterAll
		void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class InstancePerMethodOuterTestCase {

		@SuppressWarnings("unused")
		InstancePerMethodOuterTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		// The following is commented out b/c it's the default.
		// @TestInstance(Lifecycle.PER_METHOD)
		class NestedInstancePerMethodTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerMethodTestCase() {
				nestedInstanceCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}
		}
	}

	@ExtendWith(InstanceTrackingExtension.class)
	@TestInstance(Lifecycle.PER_CLASS)
	private static class InstancePerClassOuterTestCase {

		@SuppressWarnings("unused")
		InstancePerClassOuterTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerClassTestCase() {
				nestedInstanceCount++;
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class MixedLifecyclesOuterTestCase {

		@SuppressWarnings("unused")
		MixedLifecyclesOuterTestCase() {
			instanceCount++;
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void outerTest() {
			assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerClassTestCase() {
				nestedInstanceCount++;
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				lastTestInvoked = testInfo.getTestMethod().get().getName();
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

	// Intentionally not implementing BeforeTestExecutionCallback, AfterTestExecutionCallback,
	// and TestExecutionExceptionHandler, since they are analogous to BeforeEachCallback and
	// AfterEachCallback with regard to instance scope.
	private static class InstanceTrackingExtension implements ContainerExecutionCondition, TestInstancePostProcessor,
			BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

		@Override
		public ConditionEvaluationResult evaluateContainerExecutionCondition(ContainerExtensionContext context) {
			instanceMap.put(containerExecutionConditionKey(context.getTestClass().get()),
				context.getTestInstance().orElse(null));

			return ConditionEvaluationResult.enabled("enigma");
		}

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			assertNotNull(testInstance);
			context.getTestInstance().ifPresent(instance -> assertSame(testInstance, instance));
			instanceMap.put(postProcessTestInstanceKey(context.getTestClass().get()), testInstance);
		}

		@Override
		public void beforeAll(ContainerExtensionContext context) {
			instanceMap.put(beforeAllCallbackKey(context.getTestClass().get()), context.getTestInstance().orElse(null));
		}

		@Override
		public void afterAll(ContainerExtensionContext context) {
			instanceMap.put(afterAllCallbackKey(context.getTestClass().get()), context.getTestInstance().orElse(null));
		}

		@Override
		public void beforeEach(TestExtensionContext context) {
			instanceMap.put(
				beforeEachCallbackKey(context.getTestClass().get(), context.getTestMethod().get().getName()),
				context.getTestInstance().orElse(null));
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			instanceMap.put(afterEachCallbackKey(context.getTestClass().get(), context.getTestMethod().get().getName()),
				context.getTestInstance().orElse(null));
		}

	}

}
