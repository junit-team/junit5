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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.abortedWithReason;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.displayName;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.skippedWithReason;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.opentest4j.AssertionFailedError;

public class TestTemplateInvocationTests extends AbstractJupiterTestEngineTests {

	@Test
	void templateWithoutRegisteredExtensionReportsFailure() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithoutRegisteredExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithoutRegisteredExtension"), started()), //
				event(container("templateWithoutRegisteredExtension"), finishedWithFailure(
					message("You must register at least one TestTemplateInvocationContextProvider for this method")))));
	}

	@Test
	void templateWithSingleRegisteredExtensionIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithSingleRegisteredExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithSingleRegisteredExtension"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithSingleRegisteredExtension"), finishedSuccessfully())));
	}

	@Test
	void parentChildRelationshipIsEstablished() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithSingleRegisteredExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		TestDescriptor templateMethodDescriptor = findTestDescriptor(eventRecorder,
			container("templateWithSingleRegisteredExtension"));
		TestDescriptor invocationDescriptor = findTestDescriptor(eventRecorder, test("test-template-invocation:#1"));
		assertThat(invocationDescriptor.getParent()).hasValue(templateMethodDescriptor);
		assertThat(templateMethodDescriptor.getChildren()).isEqualTo(singleton(invocationDescriptor));
	}

	@Test
	void beforeAndAfterEachMethodsAreExecutedAroundInvocation() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(TestTemplateTestClassWithBeforeAndAfterEach.class, "testTemplateWithTwoInvocations")).build();

		executeTests(request);

		assertThat(TestTemplateTestClassWithBeforeAndAfterEach.lifecycleEvents).containsExactly("before:[1]",
			"after:[1]", "before:[2]", "after:[2]");
	}

	@Test
	void templateWithTwoRegisteredExtensionsIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithTwoRegisteredExtensions")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithTwoRegisteredExtensions"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1"), displayName("[1]")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(dynamicTestRegistered("test-template-invocation:#2"), displayName("[2]")), //
				event(test("test-template-invocation:#2"), started()), //
				event(test("test-template-invocation:#2"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithTwoRegisteredExtensions"), finishedSuccessfully())));
	}

	@Test
	void templateWithTwoInvocationsFromSingleExtensionIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithTwoInvocationsFromSingleExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithTwoInvocationsFromSingleExtension"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1"), displayName("[1]")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(dynamicTestRegistered("test-template-invocation:#2"), displayName("[2]")), //
				event(test("test-template-invocation:#2"), started()), //
				event(test("test-template-invocation:#2"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithTwoInvocationsFromSingleExtension"), finishedSuccessfully())));
	}

	@Test
	void templateWithDisabledInvocationsIsSkipped() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithDisabledInvocations")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithDisabledInvocations"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1")), //
				event(test("test-template-invocation:#1"), skippedWithReason("tests are always disabled")), //
				event(container("templateWithDisabledInvocations"), finishedSuccessfully())));
	}

	@Test
	void disabledTemplateIsSkipped() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "disabledTemplate")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("disabledTemplate"), skippedWithReason("containers are always disabled"))));
	}

	@Test
	void templateWithCustomizedDisplayNamesIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithCustomizedDisplayNames")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithCustomizedDisplayNames"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1"),
					displayName("1 --> templateWithCustomizedDisplayNames()")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"),
					finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithCustomizedDisplayNames"), finishedSuccessfully())));
	}

	@Test
	void templateWithDynamicParameterResolverIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(MyTestTemplateTestCase.class,
			"templateWithDynamicParameterResolver", "java.lang.String")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithDynamicParameterResolver"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1"), displayName("[1] foo")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"), finishedWithFailure(message("foo"))), //
				event(dynamicTestRegistered("test-template-invocation:#2"), displayName("[2] bar")), //
				event(test("test-template-invocation:#2"), started()), //
				event(test("test-template-invocation:#2"), finishedWithFailure(message("bar"))), //
				event(container("templateWithDynamicParameterResolver"), finishedSuccessfully())));
	}

	@Test
	void templateWithDynamicTestInstancePostProcessorIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithDynamicTestInstancePostProcessor")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithDynamicTestInstancePostProcessor"), started()), //
				event(dynamicTestRegistered("test-template-invocation:#1")), //
				event(test("test-template-invocation:#1"), started()), //
				event(test("test-template-invocation:#1"), finishedWithFailure(message("foo"))), //
				event(dynamicTestRegistered("test-template-invocation:#2")), //
				event(test("test-template-invocation:#2"), started()), //
				event(test("test-template-invocation:#2"), finishedWithFailure(message("bar"))), //
				event(container("templateWithDynamicTestInstancePostProcessor"), finishedSuccessfully())));
	}

	@Test
	void lifecycleCallbacksAreExecutedForInvocation() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(TestTemplateTestClassWithDynamicLifecycleCallbacks.class)).build();

		executeTests(request);

		// @formatter:off
		assertThat(TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents).containsExactly(
			"beforeEach",
				"beforeTestExecution",
					"testTemplate:foo",
					"handleTestExecutionException",
				"afterTestExecution",
			"afterEach",
			"beforeEach",
				"beforeTestExecution",
					"testTemplate:bar",
				"afterTestExecution",
			"afterEach");
		// @formatter:on
	}

	@Test
	void extensionIsAskedForSupportBeforeItMustProvide() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithWrongParameterType", int.class.getName())).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithWrongParameterType"), started()), //
				event(container("templateWithWrongParameterType"), finishedWithFailure(message(
					"You must register at least one TestTemplateInvocationContextProvider that supports this method")))));
	}

	@Test
	void templateWithSupportingProviderButNoInvocationsReportsAbortedTest() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithSupportingProviderButNoInvocations")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithSupportingProviderButNoInvocations"), started()), //
				event(container("templateWithSupportingProviderButNoInvocations"), abortedWithReason(
					message("No supporting TestTemplateInvocationContextProvider provided an invocation context")))));
	}

	private TestDescriptor findTestDescriptor(ExecutionEventRecorder eventRecorder,
			Condition<ExecutionEvent> condition) {
		// @formatter:off
		return eventRecorder.eventStream()
				.filter(condition::matches)
				.findAny()
				.map(ExecutionEvent::getTestDescriptor)
				.orElseThrow(() -> new AssertionFailedError("Could not find execution event for condition: " + condition));
		// @formatter:on
	}

	@SuppressWarnings({ "unchecked", "varargs", "rawtypes" })
	@SafeVarargs
	private final Condition<? super ExecutionEvent>[] wrappedInContainerEvents(Class<MyTestTemplateTestCase> clazz,
			Condition<? super ExecutionEvent>... wrappedConditions) {
		List<Condition<? super ExecutionEvent>> conditions = new ArrayList<>();
		conditions.add(event(engine(), started()));
		conditions.add(event(container(clazz), started()));
		conditions.addAll(asList(wrappedConditions));
		conditions.add(event(container(clazz), finishedSuccessfully()));
		conditions.add(event(engine(), finishedSuccessfully()));
		return conditions.toArray(new Condition[0]);
	}

	static class MyTestTemplateTestCase {

		@Test
		void foo() {
		}

		@TestTemplate
		void templateWithoutRegisteredExtension() {
		}

		@ExtendWith(SingleInvocationContextProvider.class)
		@TestTemplate
		void templateWithSingleRegisteredExtension() {
			fail("invocation is expected to fail");
		}

		@ExtendWith({ SingleInvocationContextProvider.class,
				AnotherInvocationContextProviderWithASingleInvocation.class })
		@TestTemplate
		void templateWithTwoRegisteredExtensions() {
			fail("invocation is expected to fail");
		}

		@ExtendWith(TwoInvocationsContextProvider.class)
		@TestTemplate
		void templateWithTwoInvocationsFromSingleExtension() {
			fail("invocation is expected to fail");
		}

		@ExtendWith({ SingleInvocationContextProvider.class, AlwaysDisabledTestExecutionCondition.class })
		@TestTemplate
		void templateWithDisabledInvocations() {
			fail("this is never called");
		}

		@ExtendWith(AlwaysDisabledContainerExecutionCondition.class)
		@TestTemplate
		void disabledTemplate() {
			fail("this is never called");
		}

		@ExtendWith(InvocationContextProviderWithCustomizedDisplayNames.class)
		@TestTemplate
		void templateWithCustomizedDisplayNames() {
			fail("invocation is expected to fail");
		}

		@ExtendWith(StringParameterResolvingInvocationContextProvider.class)
		@TestTemplate
		void templateWithDynamicParameterResolver(String parameter) {
			fail(parameter);
		}

		@ExtendWith(StringParameterResolvingInvocationContextProvider.class)
		@TestTemplate
		void templateWithWrongParameterType(int parameter) {
			fail("never called");
		}

		private String parameterInstanceVariable;

		@ExtendWith(StringParameterInjectingInvocationContextProvider.class)
		@TestTemplate
		void templateWithDynamicTestInstancePostProcessor() {
			fail(parameterInstanceVariable);
		}

		@ExtendWith(InvocationContextProviderThatSupportsEverythingButProvidesNothing.class)
		@TestTemplate
		void templateWithSupportingProviderButNoInvocations() {
			fail("never called");
		}

	}

	static class TestTemplateTestClassWithBeforeAndAfterEach {

		private static List<String> lifecycleEvents = new ArrayList<>();

		@BeforeEach
		void beforeEach(TestInfo testInfo) {
			lifecycleEvents.add("before:" + testInfo.getDisplayName());
		}

		@AfterEach
		void afterEach(TestInfo testInfo) {
			lifecycleEvents.add("after:" + testInfo.getDisplayName());
		}

		@ExtendWith(TwoInvocationsContextProvider.class)
		@TestTemplate
		void testTemplateWithTwoInvocations() {
			fail("invocation is expected to fail");
		}
	}

	static class TestTemplateTestClassWithDynamicLifecycleCallbacks {

		private static List<String> lifecycleEvents = new ArrayList<>();

		@ExtendWith(InvocationContextProviderWithDynamicLifecycleCallbacks.class)
		@TestTemplate
		void testTemplate(TestInfo testInfo) {
			lifecycleEvents.add("testTemplate:" + testInfo.getDisplayName());
			assertEquals("bar", testInfo.getDisplayName());
		}
	}

	private static class SingleInvocationContextProvider implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return singleton(emptyTestTemplateInvocationContext()).iterator();
		}
	}

	private static class AnotherInvocationContextProviderWithASingleInvocation
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return singleton(emptyTestTemplateInvocationContext()).iterator();
		}
	}

	private static class TwoInvocationsContextProvider implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return asList(emptyTestTemplateInvocationContext(), emptyTestTemplateInvocationContext()).iterator();
		}
	}

	private static class AlwaysDisabledTestExecutionCondition implements TestExecutionCondition {
		@Override
		public ConditionEvaluationResult evaluate(TestExtensionContext context) {
			return ConditionEvaluationResult.disabled("tests are always disabled");
		}
	}

	private static class AlwaysDisabledContainerExecutionCondition implements ContainerExecutionCondition {
		@Override
		public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
			return ConditionEvaluationResult.disabled("containers are always disabled");
		}
	}

	private static class InvocationContextProviderWithCustomizedDisplayNames
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return Stream.<TestTemplateInvocationContext> generate(() -> new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return invocationIndex + " --> " + context.getDisplayName();
				}
			}).limit(1).iterator();
		}
	}

	private static class StringParameterResolvingInvocationContextProvider
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			// @formatter:off
			return context.getTestMethod()
				.map(Method::getParameterTypes)
				.map(Arrays::stream)
				.map(parameters -> parameters.anyMatch(Predicate.isEqual(String.class)))
				.orElse(false);
			// @formatter:on
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return asList(createContext("foo"), createContext("bar")).iterator();
		}

		private TestTemplateInvocationContext createContext(String argument) {
			return new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return TestTemplateInvocationContext.super.getDisplayName(invocationIndex) + " " + argument;
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return singletonList(new ParameterResolver() {
						@Override
						public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
								throws ParameterResolutionException {
							return true;
						}

						@Override
						public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
								throws ParameterResolutionException {
							return argument;
						}
					});
				}
			};
		}
	}

	private static class StringParameterInjectingInvocationContextProvider
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return asList(createContext("foo"), createContext("bar")).iterator();
		}

		private TestTemplateInvocationContext createContext(String argument) {
			return new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return TestTemplateInvocationContext.super.getDisplayName(invocationIndex) + " " + argument;
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return singletonList((TestInstancePostProcessor) (testInstance, context) -> {
						Field field = testInstance.getClass().getDeclaredField("parameterInstanceVariable");
						field.setAccessible(true);
						field.set(testInstance, argument);
					});
				}
			};
		}
	}

	private static class InvocationContextProviderWithDynamicLifecycleCallbacks
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return asList(createContext("foo"), createContext("bar")).iterator();
		}

		private TestTemplateInvocationContext createContext(String argument) {
			return new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return argument;
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return singletonList(new LifecycleCallbackExtension());
				}
			};
		}

		private static class LifecycleCallbackExtension implements BeforeEachCallback, BeforeTestExecutionCallback,
				TestExecutionExceptionHandler, AfterTestExecutionCallback, AfterEachCallback {

			@Override
			public void beforeEach(TestExtensionContext context) throws Exception {
				TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents.add("beforeEach");
			}

			@Override
			public void beforeTestExecution(TestExtensionContext context) throws Exception {
				TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents.add("beforeTestExecution");
			}

			@Override
			public void handleTestExecutionException(TestExtensionContext context, Throwable throwable)
					throws Throwable {
				TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents.add("handleTestExecutionException");
				throw throwable;
			}

			@Override
			public void afterTestExecution(TestExtensionContext context) throws Exception {
				TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents.add("afterTestExecution");
			}

			@Override
			public void afterEach(TestExtensionContext context) throws Exception {
				TestTemplateTestClassWithDynamicLifecycleCallbacks.lifecycleEvents.add("afterEach");
			}
		}
	}

	private static class InvocationContextProviderThatSupportsEverythingButProvidesNothing
			implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supports(ContainerExtensionContext context) {
			return true;
		}

		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return emptyIterator();
		}
	}

	private static TestTemplateInvocationContext emptyTestTemplateInvocationContext() {
		return new TestTemplateInvocationContext() {
		};
	}
}
