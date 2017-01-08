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
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestExtensionContext;
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
				event(container("templateWithoutRegisteredExtension"), finishedWithFailure(message(
					"You need to register at least one TestTemplateInvocationContextProvider for this method")))));
	}

	@Test
	void templateWithSingleRegisteredExtensionIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithSingleRegisteredExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithSingleRegisteredExtension"), started()), //
				event(dynamicTestRegistered("template-invocation:#1")), //
				event(test("template-invocation:#1"), started()), //
				event(test("template-invocation:#1"), finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithSingleRegisteredExtension"), finishedSuccessfully())));

		TestDescriptor templateMethodDescriptor = findTestDescriptor(eventRecorder,
			container("templateWithSingleRegisteredExtension"));
		TestDescriptor invocationDescriptor = findTestDescriptor(eventRecorder, test("template-invocation:#1"));
		assertThat(invocationDescriptor.getParent()).hasValue(templateMethodDescriptor);
		assertThat(templateMethodDescriptor.getChildren()).isEqualTo(singleton(invocationDescriptor));
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

	@Test
	void templateWithTwoRegisteredExtensionsIsInvoked() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithTwoRegisteredExtensions")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			wrappedInContainerEvents(MyTestTemplateTestCase.class, //
				event(container("templateWithTwoRegisteredExtensions"), started()), //
				event(dynamicTestRegistered("template-invocation:#1"), displayName("[1]")), //
				event(test("template-invocation:#1"), started()), //
				event(test("template-invocation:#1"), finishedWithFailure(message("invocation is expected to fail"))), //
				event(dynamicTestRegistered("template-invocation:#2"), displayName("[2]")), //
				event(test("template-invocation:#2"), started()), //
				event(test("template-invocation:#2"), finishedWithFailure(message("invocation is expected to fail"))), //
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
				event(dynamicTestRegistered("template-invocation:#1"), displayName("[1]")), //
				event(test("template-invocation:#1"), started()), //
				event(test("template-invocation:#1"), finishedWithFailure(message("invocation is expected to fail"))), //
				event(dynamicTestRegistered("template-invocation:#2"), displayName("[2]")), //
				event(test("template-invocation:#2"), started()), //
				event(test("template-invocation:#2"), finishedWithFailure(message("invocation is expected to fail"))), //
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
				event(dynamicTestRegistered("template-invocation:#1")), //
				event(test("template-invocation:#1"), skippedWithReason("tests are always disabled")), //
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
				event(dynamicTestRegistered("template-invocation:#1"),
					displayName("1 --> templateWithCustomizedDisplayNames()")), //
				event(test("template-invocation:#1"), started()), //
				event(test("template-invocation:#1"), finishedWithFailure(message("invocation is expected to fail"))), //
				event(container("templateWithCustomizedDisplayNames"), finishedSuccessfully())));
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

	}

	private static class SingleInvocationContextProvider implements TestTemplateInvocationContextProvider {
		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return singleton(emptyTestTemplateInvocationContext()).iterator();
		}
	}

	private static class AnotherInvocationContextProviderWithASingleInvocation
			implements TestTemplateInvocationContextProvider {
		@Override
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return singleton(emptyTestTemplateInvocationContext()).iterator();
		}
	}

	private static class TwoInvocationsContextProvider implements TestTemplateInvocationContextProvider {
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
		public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
			return Stream.<TestTemplateInvocationContext> generate(() -> new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return invocationIndex + " --> " + context.getDisplayName();
				}
			}).limit(1).iterator();
		}
	}

	private static TestTemplateInvocationContext emptyTestTemplateInvocationContext() {
		return new TestTemplateInvocationContext() {
		};
	}
}
