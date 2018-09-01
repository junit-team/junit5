/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Integration tests for {@link TestInstance @TestInstance} lifecycle support.
 *
 * @since 5.0
 * @see TestInstanceLifecycleConfigurationTests
 * @see TestInstanceLifecycleKotlinTests
 */
class TestInstanceLifecycleTests extends AbstractJupiterTestEngineTests {

	private static final Map<Class<?>, List<Lifecycle>> lifecyclesMap = new LinkedHashMap<>();
	private static final Map<String, Object> instanceMap = new LinkedHashMap<>();
	private static final List<String> testsInvoked = new ArrayList<>();
	private static final Map<Class<?>, Integer> instanceCount = new LinkedHashMap<>();

	private static int beforeAllCount;
	private static int afterAllCount;
	private static int beforeEachCount;
	private static int afterEachCount;

	@BeforeEach
	void init() {
		lifecyclesMap.clear();
		instanceMap.clear();
		testsInvoked.clear();
		instanceCount.clear();
		beforeAllCount = 0;
		afterAllCount = 0;
		beforeEachCount = 0;
		afterEachCount = 0;
	}

	@Test
	void instancePerMethod() {
		Class<?> testClass = InstancePerMethodTestCase.class;
		int containers = 3;
		int tests = 3;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(InstancePerMethodTestCase.class, 3));
		int allMethods = 1;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String testTemplateKey = testTemplateKey(testClass, "singletonTest");
		String testExecutionConditionKey1 = executionConditionKey(testClass, testsInvoked.get(0));
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, testsInvoked.get(0));
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, testsInvoked.get(0));
		String testExecutionConditionKey2 = executionConditionKey(testClass, testsInvoked.get(1));
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, testsInvoked.get(1));
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, testsInvoked.get(1));
		String testExecutionConditionKey3 = executionConditionKey(testClass, testsInvoked.get(2));
		String beforeEachCallbackKey3 = beforeEachCallbackKey(testClass, testsInvoked.get(2));
		String afterEachCallbackKey3 = afterEachCallbackKey(testClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				postProcessTestInstanceKey,
				testTemplateKey,
				testExecutionConditionKey1,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				testExecutionConditionKey2,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				testExecutionConditionKey3,
				beforeEachCallbackKey3,
				afterEachCallbackKey3,
				afterAllCallbackKey
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		Object instance = instanceMap.get(beforeEachCallbackKey1);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey1));
		assertSame(instance, instanceMap.get(testExecutionConditionKey1));

		instance = instanceMap.get(beforeEachCallbackKey2);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey2));
		assertSame(instance, instanceMap.get(testExecutionConditionKey2));

		instance = instanceMap.get(beforeEachCallbackKey3);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterEachCallbackKey3));
		assertSame(instance, instanceMap.get(testExecutionConditionKey3));
		assertSame(instance, instanceMap.get(postProcessTestInstanceKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
	}

	@Test
	void instancePerClass() {
		instancePerClass(InstancePerClassTestCase.class, instanceCounts(entry(InstancePerClassTestCase.class, 1)));
	}

	@Test
	void instancePerClassWithInheritedLifecycleMode() {
		instancePerClass(SubInstancePerClassTestCase.class,
			instanceCounts(entry(SubInstancePerClassTestCase.class, 1)));
	}

	private void instancePerClass(Class<?> testClass, Map.Entry<Class<?>, Integer>[] instances) {
		int containers = 3;
		int tests = 3;
		int allMethods = 2;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String testTemplateKey = testTemplateKey(testClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String testExecutionConditionKey1 = executionConditionKey(testClass, testsInvoked.get(0));
		String beforeEachCallbackKey1 = beforeEachCallbackKey(testClass, testsInvoked.get(0));
		String afterEachCallbackKey1 = afterEachCallbackKey(testClass, testsInvoked.get(0));
		String testExecutionConditionKey2 = executionConditionKey(testClass, testsInvoked.get(1));
		String beforeEachCallbackKey2 = beforeEachCallbackKey(testClass, testsInvoked.get(1));
		String afterEachCallbackKey2 = afterEachCallbackKey(testClass, testsInvoked.get(1));
		String testExecutionConditionKey3 = executionConditionKey(testClass, testsInvoked.get(2));
		String beforeEachCallbackKey3 = beforeEachCallbackKey(testClass, testsInvoked.get(2));
		String afterEachCallbackKey3 = afterEachCallbackKey(testClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				postProcessTestInstanceKey,
				containerExecutionConditionKey,
				beforeAllCallbackKey,
				testTemplateKey,
				testExecutionConditionKey1,
				beforeEachCallbackKey1,
				afterEachCallbackKey1,
				testExecutionConditionKey2,
				beforeEachCallbackKey2,
				afterEachCallbackKey2,
				testExecutionConditionKey3,
				beforeEachCallbackKey3,
				afterEachCallbackKey3,
				afterAllCallbackKey
		);
		// @formatter:on

		Object instance = instanceMap.get(beforeAllCallbackKey);
		assertNotNull(instance);
		assertSame(instance, instanceMap.get(afterAllCallbackKey));
		assertSame(instance, instanceMap.get(testExecutionConditionKey1));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey1));
		assertSame(instance, instanceMap.get(afterEachCallbackKey1));
		assertSame(instance, instanceMap.get(testExecutionConditionKey2));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey2));
		assertSame(instance, instanceMap.get(afterEachCallbackKey2));
		assertSame(instance, instanceMap.get(testExecutionConditionKey3));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey3));
		assertSame(instance, instanceMap.get(afterEachCallbackKey3));
		assertSame(instance, instanceMap.get(postProcessTestInstanceKey));
		assertNull(instanceMap.get(containerExecutionConditionKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	@Test
	void instancePerMethodWithNestedTestClass() {
		Class<?> testClass = InstancePerMethodOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerMethodOuterTestCase.NestedInstancePerMethodTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 4), entry(nestedTestClass, 3));
		int allMethods = 1;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
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
		assertSame(instance, instanceMap.get(outerTestExecutionConditionKey));

		Object nestedInstance1 = instanceMap.get(nestedBeforeEachCallbackKey1);
		assertNotNull(nestedInstance1);
		assertNotSame(instance, nestedInstance1);
		assertSame(nestedInstance1, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstance1, instanceMap.get(nestedExecutionConditionKey1));

		Object nestedInstance2 = instanceMap.get(nestedBeforeEachCallbackKey2);
		assertNotNull(nestedInstance2);
		assertNotSame(instance, nestedInstance2);
		assertNotSame(nestedInstance1, nestedInstance2);
		assertSame(nestedInstance2, instanceMap.get(nestedAfterEachCallbackKey2));
		assertSame(nestedInstance2, instanceMap.get(nestedExecutionConditionKey2));

		Object nestedInstance3 = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance3);
		assertNotSame(instance, nestedInstance3);
		assertNotSame(nestedInstance1, nestedInstance3);
		assertSame(nestedInstance3, instanceMap.get(nestedAfterEachCallbackKey3));
		assertSame(nestedInstance3, instanceMap.get(nestedExecutionConditionKey3));

		Object outerInstance1 = ReflectionUtils.getOutermostInstance(nestedInstance1, testClass).get();
		Object outerInstance2 = ReflectionUtils.getOutermostInstance(nestedInstance2, testClass).get();
		Object outerInstance3 = ReflectionUtils.getOutermostInstance(nestedInstance3, testClass).get();
		assertNotSame(outerInstance1, outerInstance2);
		assertNotSame(outerInstance1, outerInstance3);

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class for
		// test2().
		assertSame(outerInstance3, instanceMap.get(postProcessTestInstanceKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
	}

	@Test
	void instancePerClassWithNestedTestClass() {
		Class<?> testClass = InstancePerClassOuterTestCase.class;
		Class<?> nestedTestClass = InstancePerClassOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 1), entry(nestedTestClass, 1));
		int allMethods = 2;
		int eachMethods = 3;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
		);
		// @formatter:on

		Object instance = instanceMap.get(postProcessTestInstanceKey);
		assertNotNull(instance);
		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertSame(instance, instanceMap.get(beforeAllCallbackKey));
		assertSame(instance, instanceMap.get(afterAllCallbackKey));
		assertSame(instance, instanceMap.get(outerTestExecutionConditionKey));
		assertSame(instance, instanceMap.get(beforeEachCallbackKey));
		assertSame(instance, instanceMap.get(afterEachCallbackKey));

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance);
		assertNotSame(instance, nestedInstance);
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey1));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey2));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey3));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey3));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey3));

		Object outerInstance = ReflectionUtils.getOutermostInstance(nestedInstance, testClass).get();
		assertSame(outerInstance, instanceMap.get(postProcessTestInstanceKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	@Test
	void instancePerMethodOnOuterTestClassWithInstancePerClassOnNestedTestClass() {
		Class<?> testClass = MixedLifecyclesOuterTestCase.class;
		Class<?> nestedTestClass = MixedLifecyclesOuterTestCase.NestedInstancePerClassTestCase.class;
		int containers = 4;
		int tests = 4;
		Map.Entry<Class<?>, Integer>[] instances = instanceCounts(entry(testClass, 2), entry(nestedTestClass, 1));
		int allMethods = 1;
		int eachMethods = 7;

		performAssertions(testClass, containers, tests, instances, allMethods, eachMethods);

		String containerExecutionConditionKey = executionConditionKey(testClass, null);
		String nestedContainerExecutionConditionKey = executionConditionKey(nestedTestClass, null);
		String nestedTestTemplateKey = testTemplateKey(nestedTestClass, "singletonTest");
		String postProcessTestInstanceKey = postProcessTestInstanceKey(testClass);
		String nestedPostProcessTestInstanceKey = postProcessTestInstanceKey(nestedTestClass);
		String beforeAllCallbackKey = beforeAllCallbackKey(testClass);
		String afterAllCallbackKey = afterAllCallbackKey(testClass);
		String outerTestExecutionConditionKey = executionConditionKey(testClass, "outerTest");
		String beforeEachCallbackKey = beforeEachCallbackKey(testClass, "outerTest");
		String afterEachCallbackKey = afterEachCallbackKey(testClass, "outerTest");
		String nestedBeforeAllCallbackKey = beforeAllCallbackKey(nestedTestClass);
		String nestedAfterAllCallbackKey = afterAllCallbackKey(nestedTestClass);
		String nestedExecutionConditionKey1 = executionConditionKey(nestedTestClass, testsInvoked.get(0));
		String nestedBeforeEachCallbackKey1 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedAfterEachCallbackKey1 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(0));
		String nestedExecutionConditionKey2 = executionConditionKey(nestedTestClass, testsInvoked.get(1));
		String nestedBeforeEachCallbackKey2 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedAfterEachCallbackKey2 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(1));
		String nestedExecutionConditionKey3 = executionConditionKey(nestedTestClass, testsInvoked.get(2));
		String nestedBeforeEachCallbackKey3 = beforeEachCallbackKey(nestedTestClass, testsInvoked.get(2));
		String nestedAfterEachCallbackKey3 = afterEachCallbackKey(nestedTestClass, testsInvoked.get(2));

		// @formatter:off
		assertThat(instanceMap.keySet()).containsExactlyInAnyOrder(
				containerExecutionConditionKey,
				nestedTestTemplateKey,
				nestedContainerExecutionConditionKey,
				postProcessTestInstanceKey,
				nestedPostProcessTestInstanceKey,
				beforeAllCallbackKey,
				afterAllCallbackKey,
				outerTestExecutionConditionKey,
				beforeEachCallbackKey,
				afterEachCallbackKey,
				nestedBeforeAllCallbackKey,
				nestedAfterAllCallbackKey,
				nestedExecutionConditionKey1,
				nestedBeforeEachCallbackKey1,
				nestedAfterEachCallbackKey1,
				nestedExecutionConditionKey2,
				nestedBeforeEachCallbackKey2,
				nestedAfterEachCallbackKey2,
				nestedExecutionConditionKey3,
				nestedBeforeEachCallbackKey3,
				nestedAfterEachCallbackKey3
		);
		// @formatter:on

		assertNull(instanceMap.get(containerExecutionConditionKey));
		assertNull(instanceMap.get(beforeAllCallbackKey));
		assertNull(instanceMap.get(afterAllCallbackKey));

		Object instance = instanceMap.get(beforeEachCallbackKey);
		assertSame(instance, instanceMap.get(afterEachCallbackKey));
		assertSame(instance, instanceMap.get(outerTestExecutionConditionKey));

		Object nestedInstance = instanceMap.get(nestedPostProcessTestInstanceKey);
		assertNotNull(nestedInstance);
		assertNotSame(instance, nestedInstance);
		assertNull(instanceMap.get(nestedContainerExecutionConditionKey));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedAfterAllCallbackKey));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey1));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey1));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey2));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey2));
		assertSame(nestedInstance, instanceMap.get(nestedExecutionConditionKey3));
		assertSame(nestedInstance, instanceMap.get(nestedBeforeEachCallbackKey3));
		assertSame(nestedInstance, instanceMap.get(nestedAfterEachCallbackKey3));

		// The last tracked instance stored under postProcessTestInstanceKey
		// is only created in order to instantiate the nested test class.
		Object outerInstance = ReflectionUtils.getOutermostInstance(nestedInstance, testClass).get();
		assertEquals(instance.getClass(), outerInstance.getClass());
		assertNotSame(instance, outerInstance);
		assertSame(outerInstance, instanceMap.get(postProcessTestInstanceKey));

		assertThat(lifecyclesMap.keySet()).containsExactly(testClass, nestedTestClass);
		assertThat(lifecyclesMap.get(testClass).stream()).allMatch(Lifecycle.PER_METHOD::equals);
		assertThat(lifecyclesMap.get(nestedTestClass).stream()).allMatch(Lifecycle.PER_CLASS::equals);
	}

	private void performAssertions(Class<?> testClass, int containers, int tests,
			Map.Entry<Class<?>, Integer>[] instanceCountEntries, int allMethods, int eachMethods) {

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		// @formatter:off
		assertAll(
			() -> assertEquals(containers, eventRecorder.getContainerStartedCount(), "# containers started"),
			() -> assertEquals(containers, eventRecorder.getContainerFinishedCount(), "# containers finished"),
			() -> assertEquals(tests, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(tests, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertThat(instanceCount).describedAs("instance count").contains(instanceCountEntries),
			() -> assertEquals(allMethods, beforeAllCount, "@BeforeAll count"),
			() -> assertEquals(allMethods, afterAllCount, "@AfterAll count"),
			() -> assertEquals(eachMethods, beforeEachCount, "@BeforeEach count"),
			() -> assertEquals(eachMethods, afterEachCount, "@AfterEach count")
		);
		// @formatter:on
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private final Map.Entry<Class<?>, Integer>[] instanceCounts(Map.Entry<Class<?>, Integer>... entries) {
		return entries;
	}

	private static void incrementInstanceCount(Class<?> testClass) {
		instanceCount.compute(testClass, (key, value) -> value == null ? 1 : value + 1);
	}

	private static String executionConditionKey(Class<?> testClass, String testMethod) {
		return concat(ExecutionCondition.class, testClass, testMethod);
	}

	private static String postProcessTestInstanceKey(Class<?> testClass) {
		return concat(TestInstancePostProcessor.class, testClass);
	}

	private static String beforeAllCallbackKey(Class<?> testClass) {
		return concat(BeforeAllCallback.class, testClass);
	}

	private static String afterAllCallbackKey(Class<?> testClass) {
		return concat(AfterAllCallback.class, testClass);
	}

	private static String beforeEachCallbackKey(Class<?> testClass, String testMethod) {
		return concat(BeforeEachCallback.class, testClass, testMethod);
	}

	private static String afterEachCallbackKey(Class<?> testClass, String testMethod) {
		return concat(AfterEachCallback.class, testClass, testMethod);
	}

	private static String testTemplateKey(Class<?> testClass, String testMethod) {
		return concat(TestTemplateInvocationContextProvider.class, testClass, testMethod);
	}

	private static String concat(Class<?> c1, Class<?> c2, String str) {
		return concat(c1.getSimpleName(), c2.getSimpleName(), str);
	}

	private static String concat(Class<?> c1, Class<?> c2) {
		return concat(c1.getSimpleName(), c2.getSimpleName());
	}

	private static String concat(String... args) {
		return join(".", args);
	}

	// -------------------------------------------------------------------------

	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	static class InstancePerMethodTestCase {

		InstancePerMethodTestCase() {
			incrementInstanceCount(InstancePerMethodTestCase.class);
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
			testsInvoked.add(testInfo.getTestMethod().get().getName());
		}

		@Test
		void test2(TestInfo testInfo) {
			testsInvoked.add(testInfo.getTestMethod().get().getName());
		}

		@SingletonTest
		void singletonTest(TestInfo testInfo) {
			testsInvoked.add(testInfo.getTestMethod().get().getName());
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
	static class InstancePerClassTestCase extends InstancePerMethodTestCase {

		InstancePerClassTestCase() {
			incrementInstanceCount(InstancePerClassTestCase.class);
		}

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

	static class SubInstancePerClassTestCase extends InstancePerClassTestCase {
		SubInstancePerClassTestCase() {
			incrementInstanceCount(SubInstancePerClassTestCase.class);
		}
	}

	@ExtendWith(InstanceTrackingExtension.class)
	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	static class InstancePerMethodOuterTestCase {

		InstancePerMethodOuterTestCase() {
			incrementInstanceCount(InstancePerMethodOuterTestCase.class);
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

			NestedInstancePerMethodTestCase() {
				incrementInstanceCount(NestedInstancePerMethodTestCase.class);
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}
		}
	}

	@ExtendWith(InstanceTrackingExtension.class)
	@TestInstance(Lifecycle.PER_CLASS)
	static class InstancePerClassOuterTestCase {

		InstancePerClassOuterTestCase() {
			incrementInstanceCount(InstancePerClassOuterTestCase.class);
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

			NestedInstancePerClassTestCase() {
				incrementInstanceCount(NestedInstancePerClassTestCase.class);
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
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
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
	static class MixedLifecyclesOuterTestCase {

		MixedLifecyclesOuterTestCase() {
			incrementInstanceCount(MixedLifecyclesOuterTestCase.class);
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

			NestedInstancePerClassTestCase() {
				incrementInstanceCount(NestedInstancePerClassTestCase.class);
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
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@Test
			void test2(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
			}

			@SingletonTest
			void singletonTest(TestInfo testInfo) {
				assertSame(this, instanceMap.get(postProcessTestInstanceKey(getClass())));
				testsInvoked.add(testInfo.getTestMethod().get().getName());
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
	// AfterEachCallback with regard to instance scope and Lifecycle.
	static class InstanceTrackingExtension implements ExecutionCondition, TestInstancePostProcessor, BeforeAllCallback,
			AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestTemplateInvocationContextProvider {

		@Override
		public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
			trackLifecycle(context);
			String testMethod = context.getTestMethod().map(Method::getName).orElse(null);
			if (testMethod == null) {
				assertThat(context.getTestInstance()).isNotPresent();
				assertThat(instanceCount.getOrDefault(context.getRequiredTestClass(), 0)).isEqualTo(0);
			}
			instanceMap.put(executionConditionKey(context.getRequiredTestClass(), testMethod),
				context.getTestInstance().orElse(null));

			return ConditionEvaluationResult.enabled("enigma");
		}

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
			trackLifecycle(context);
			assertThat(context.getTestInstance()).isNotPresent();
			assertNotNull(testInstance);
			instanceMap.put(postProcessTestInstanceKey(context.getRequiredTestClass()), testInstance);
		}

		@Override
		public void beforeAll(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(beforeAllCallbackKey(context.getRequiredTestClass()),
				context.getTestInstance().orElse(null));
		}

		@Override
		public void afterAll(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(afterAllCallbackKey(context.getRequiredTestClass()),
				context.getTestInstance().orElse(null));
		}

		@Override
		public void beforeEach(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(
				beforeEachCallbackKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getRequiredTestInstance());
		}

		@Override
		public void afterEach(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(
				afterEachCallbackKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getRequiredTestInstance());
		}

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			trackLifecycle(context);
			return isAnnotated(context.getTestMethod(), SingletonTest.class);
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			trackLifecycle(context);
			instanceMap.put(testTemplateKey(context.getRequiredTestClass(), context.getRequiredTestMethod().getName()),
				context.getTestInstance().orElse(null));

			return Stream.of(new TestTemplateInvocationContext() {
			});
		}

		private static void trackLifecycle(ExtensionContext context) {
			assertThat(context.getRoot().getTestInstanceLifecycle()).isEmpty();
			lifecyclesMap.computeIfAbsent(context.getRequiredTestClass(), clazz -> new ArrayList<>()).add(
				context.getTestInstanceLifecycle().orElse(null));
		}

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@TestTemplate
	@interface SingletonTest {
	}

}
