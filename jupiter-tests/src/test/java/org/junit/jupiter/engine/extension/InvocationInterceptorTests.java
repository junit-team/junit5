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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;

class InvocationInterceptorTests extends AbstractJupiterTestEngineTests {

	@Test
	void failsTestWhenInterceptorChainDoesNotCallInvocation() {
		var results = executeTestsForClass(InvocationIgnoringInterceptorTestCase.class);

		var tests = results.testEvents().assertStatistics(stats -> stats.failed(1).succeeded(0));
		tests.failed().assertEventsMatchExactly(
			event(test("test"), finishedWithFailure(instanceOf(JUnitException.class),
				message(it -> it.startsWith("Chain of InvocationInterceptors never called invocation")))));
	}

	static class InvocationIgnoringInterceptorTestCase {
		@RegisterExtension
		Extension interceptor = new InvocationInterceptor() {
			@Override
			public void interceptTestMethod(Invocation<Void> invocation,
					ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
				// do nothing
			}
		};

		@Test
		void test() {
			// never called
		}
	}

	@Test
	void successTestWhenInterceptorChainSkippedInvocation() {
		var results = executeTestsForClass(InvocationSkippedTestCase.class);

		results.testEvents().assertStatistics(stats -> stats.failed(0).succeeded(1));
	}

	static class InvocationSkippedTestCase {
		@RegisterExtension
		Extension interceptor = new InvocationInterceptor() {
			@Override
			public void interceptTestMethod(Invocation<Void> invocation,
					ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
				invocation.skip();
			}
		};

		@Test
		void test() {
			fail("should not be called");
		}
	}

	@Test
	void failsTestWhenInterceptorChainCallsInvocationMoreThanOnce() {
		var results = executeTestsForClass(DoubleInvocationInterceptorTestCase.class);

		var tests = results.testEvents().assertStatistics(stats -> stats.failed(1).succeeded(0));
		tests.failed().assertEventsMatchExactly(
			event(test("test"), finishedWithFailure(instanceOf(JUnitException.class), message(it -> it.startsWith(
				"Chain of InvocationInterceptors called invocation multiple times instead of just once")))));
	}

	static class DoubleInvocationInterceptorTestCase {
		@RegisterExtension
		Extension interceptor = new InvocationInterceptor() {
			@Override
			public void interceptTestMethod(Invocation<Void> invocation,
					ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
					throws Throwable {
				invocation.proceed();
				invocation.proceed();
			}
		};

		@Test
		void test() {
			// called twice
		}
	}

	@TestFactory
	Stream<DynamicTest> callsInterceptors() {
		var results = executeTestsForClass(TestCaseWithThreeInterceptors.class);

		results.testEvents().assertStatistics(stats -> stats.failed(0).succeeded(3));
		return Arrays.stream(InvocationType.values()) //
				.map(invocationType -> dynamicTest(invocationType.name(), () -> {
					assertThat(getEvents(results, EnumSet.of(invocationType)).distinct()) //
							.containsExactly("before:foo", "before:bar", "before:baz", "test", "after:baz", "after:bar",
								"after:foo");
				}));
	}

	private Stream<String> getEvents(EngineExecutionResults results, EnumSet<InvocationType> types) {
		return results.allEvents().reportingEntryPublished() //
				.map(event -> event.getPayload(ReportEntry.class).orElseThrow()) //
				.map(ReportEntry::getKeyValuePairs) //
				.filter(map -> map.keySet().stream().map(InvocationType::valueOf).anyMatch(types::contains)) //
				.flatMap(map -> map.values().stream());
	}

	@ExtendWith({ FooInvocationInterceptor.class, BarInvocationInterceptor.class, BazInvocationInterceptor.class })
	static class TestCaseWithThreeInterceptors {

		public TestCaseWithThreeInterceptors(TestReporter reporter) {
			publish(reporter, InvocationType.CONSTRUCTOR);
		}

		@BeforeAll
		static void beforeAll(TestReporter reporter) {
			publish(reporter, InvocationType.BEFORE_ALL);
		}

		@BeforeEach
		void beforeEach(TestReporter reporter) {
			publish(reporter, InvocationType.BEFORE_EACH);
		}

		@Test
		void test(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_METHOD);
		}

		@RepeatedTest(1)
		void testTemplate(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_TEMPLATE_METHOD);
		}

		@TestFactory
		DynamicTest testFactory(TestReporter reporter) {
			publish(reporter, InvocationType.TEST_FACTORY_METHOD);
			return dynamicTest("dynamicTest", () -> {
				publish(reporter, InvocationType.DYNAMIC_TEST);
			});
		}

		@AfterEach
		void afterEach(TestReporter reporter) {
			publish(reporter, InvocationType.AFTER_EACH);
		}

		@AfterAll
		static void afterAll(TestReporter reporter) {
			publish(reporter, InvocationType.AFTER_ALL);
		}

		static void publish(TestReporter reporter, InvocationType type) {
			reporter.publishEntry(type.name(), "test");
		}

	}

	enum InvocationType {
		BEFORE_ALL,
		CONSTRUCTOR,
		BEFORE_EACH,
		TEST_METHOD,
		TEST_TEMPLATE_METHOD,
		TEST_FACTORY_METHOD,
		DYNAMIC_TEST,
		AFTER_EACH,
		AFTER_ALL
	}

	abstract static class ReportingInvocationInterceptor implements InvocationInterceptor {
		private final Class<TestCaseWithThreeInterceptors> testClass = TestCaseWithThreeInterceptors.class;
		private final String name;

		ReportingInvocationInterceptor(String name) {
			this.name = name;
		}

		@Override
		public void interceptBeforeAllMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).isEmpty();
			assertEquals(testClass.getDeclaredMethod("beforeAll", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.BEFORE_ALL);
		}

		@Override
		public <T> T interceptTestClassConstructor(Invocation<T> invocation,
				ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertEquals(testClass.getDeclaredConstructor(TestReporter.class), invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			return reportAndProceed(invocation, extensionContext, InvocationType.CONSTRUCTOR);
		}

		@Override
		public void interceptBeforeEachMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("beforeEach", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.BEFORE_EACH);
		}

		@Override
		public void interceptTestMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("test", TestReporter.class), invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.TEST_METHOD);
		}

		@Override
		public void interceptTestTemplateMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("testTemplate", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.TEST_TEMPLATE_METHOD);
		}

		@Override
		public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("testFactory", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			return reportAndProceed(invocation, extensionContext, InvocationType.TEST_FACTORY_METHOD);
		}

		@Override
		public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
				ExtensionContext extensionContext) throws Throwable {
			assertThat(invocationContext.getExecutable()).isNotNull();
			assertThat(extensionContext.getUniqueId()).isNotBlank();
			assertThat(extensionContext.getElement()).isEmpty();
			assertThat(extensionContext.getParent().flatMap(ExtensionContext::getTestMethod)).contains(
				testClass.getDeclaredMethod("testFactory", TestReporter.class));
			reportAndProceed(invocation, extensionContext, InvocationType.DYNAMIC_TEST);
		}

		@Override
		public void interceptAfterEachMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).containsInstanceOf(testClass);
			assertEquals(testClass.getDeclaredMethod("afterEach", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.AFTER_EACH);
		}

		@Override
		public void interceptAfterAllMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			assertEquals(testClass, invocationContext.getTargetClass());
			assertThat(invocationContext.getTarget()).isEmpty();
			assertEquals(testClass.getDeclaredMethod("afterAll", TestReporter.class),
				invocationContext.getExecutable());
			assertThat(invocationContext.getArguments()).hasSize(1).hasOnlyElementsOfType(TestReporter.class);
			reportAndProceed(invocation, extensionContext, InvocationType.AFTER_ALL);
		}

		private <T> T reportAndProceed(Invocation<T> invocation, ExtensionContext extensionContext, InvocationType type)
				throws Throwable {
			extensionContext.publishReportEntry(type.name(), "before:" + name);
			try {
				return invocation.proceed();
			}
			finally {
				extensionContext.publishReportEntry(type.name(), "after:" + name);
			}
		}
	}

	static class FooInvocationInterceptor extends ReportingInvocationInterceptor {
		FooInvocationInterceptor() {
			super("foo");
		}
	}

	static class BarInvocationInterceptor extends ReportingInvocationInterceptor {
		BarInvocationInterceptor() {
			super("bar");
		}
	}

	static class BazInvocationInterceptor extends ReportingInvocationInterceptor {
		BazInvocationInterceptor() {
			super("baz");
		}
	}

}
