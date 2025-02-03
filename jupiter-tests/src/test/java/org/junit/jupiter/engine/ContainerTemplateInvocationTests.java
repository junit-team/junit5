/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedMethod;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.Event;

/**
 * @since 5.13
 */
public class ContainerTemplateInvocationTests extends AbstractJupiterTestEngineTests {

	@Test
	void executesContainerTemplateClassTwice() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var containerTemplateId = engineId.append(ContainerTemplateTestDescriptor.SEGMENT_TYPE,
			TwoInvocationsTestCase.class.getName());
		var invocationId1 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var invocation1MethodAId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var invocation1NestedClassId = invocationId1.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocation1NestedMethodBId = invocation1NestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = containerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var invocation2MethodAId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var invocation2NestedClassId = invocationId2.append(NestedClassTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocation2NestedMethodBId = invocation2NestedClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTestsForClass(TwoInvocationsTestCase.class);

		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(containerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1MethodAId))), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedClassId))), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1MethodAId)), started()), //
			event(test(uniqueId(invocation1MethodAId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation1NestedClassId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation1NestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2MethodAId))), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedClassId))), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2MethodAId)), started()), //
			event(test(uniqueId(invocation2MethodAId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation2NestedClassId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocation2NestedClassId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(containerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesOnlySelectedMethodsDeclaredInContainerTemplate() {
		var results = executeTests(selectMethod(TwoInvocationsTestCase.class, "a"));

		results.testEvents() //
				.assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("a()")), finishedSuccessfully()));
	}

	@Test
	void executesOnlySelectedMethodsDeclaredInNestedClassOfContainerTemplate() {
		var results = executeTests(selectNestedMethod(List.of(TwoInvocationsTestCase.class),
			TwoInvocationsTestCase.NestedTestCase.class, "b"));

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("b()")), finishedSuccessfully()));
	}

	@Test
	void executesOnlyTestsPassingPostDiscoveryFilter() {
		var results = executeTests(request -> request //
				.selectors(selectClass(TwoInvocationsTestCase.class)) //
				.filters(includeTags("nested")));

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("b()")), finishedSuccessfully()));
	}

	@Test
	void prunesEmptyNestedTestClasses() {
		var results = executeTests(request -> request //
				.selectors(selectClass(TwoInvocationsTestCase.class)) //
				.filters(excludeTags("nested")));

		results.containerEvents().assertThatEvents() //
				.noneMatch(container(TwoInvocationsTestCase.NestedTestCase.class.getSimpleName())::matches);

		results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2)) //
				.assertEventsMatchLoosely(event(test(displayName("a()")), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplateClassTwiceWithClassSelectorForEnclosingClass() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var classId = engineId.append(ClassTestDescriptor.SEGMENT_TYPE,
			NestedContainerTemplateWithTwoInvocationsTestCase.class.getName());
		var methodAId = classId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "a()");
		var nestedContainerTemplateId = classId.append(ContainerTemplateTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocationId1 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var invocation1NestedMethodBId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var invocation2NestedMethodBId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTestsForClass(NestedContainerTemplateWithTwoInvocationsTestCase.class);

		results.allEvents().debug().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(classId)), started()), //

			event(test(uniqueId(methodAId)), started()), //
			event(test(uniqueId(methodAId)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), finishedSuccessfully()), //

			event(container(uniqueId(classId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplateClassTwiceWithNestedClassSelector() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var classId = engineId.append(ClassTestDescriptor.SEGMENT_TYPE,
			NestedContainerTemplateWithTwoInvocationsTestCase.class.getName());
		var nestedContainerTemplateId = classId.append(ContainerTemplateTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var invocationId1 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var invocation1NestedMethodBId = invocationId1.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");
		var invocationId2 = nestedContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var invocation2NestedMethodBId = invocationId2.append(TestMethodTestDescriptor.SEGMENT_TYPE, "b()");

		var results = executeTestsForClass(NestedContainerTemplateWithTwoInvocationsTestCase.NestedTestCase.class);

		results.allEvents().debug().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(classId)), started()), //

			event(container(uniqueId(nestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(invocationId1)), displayName("[1] A")), //
			event(container(uniqueId(invocationId1)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation1NestedMethodBId))), //
			event(test(uniqueId(invocation1NestedMethodBId)), started()), //
			event(test(uniqueId(invocation1NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId1)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(invocationId2)), displayName("[2] B")), //
			event(container(uniqueId(invocationId2)), started()), //
			event(dynamicTestRegistered(uniqueId(invocation2NestedMethodBId))), //
			event(test(uniqueId(invocation2NestedMethodBId)), started()), //
			event(test(uniqueId(invocation2NestedMethodBId)), finishedSuccessfully()), //
			event(container(uniqueId(invocationId2)), finishedSuccessfully()), //

			event(container(uniqueId(nestedContainerTemplateId)), finishedSuccessfully()), //

			event(container(uniqueId(classId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesNestedContainerTemplatesTwiceEach() {
		var engineId = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
		var outerContainerTemplateId = engineId.append(ContainerTemplateTestDescriptor.SEGMENT_TYPE,
			TwoTimesTwoInvocationsTestCase.class.getName());

		var outerInvocation1Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#1");
		var outerInvocation1NestedContainerTemplateId = outerInvocation1Id.append(
			ContainerTemplateTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation1InnerInvocation1Id = outerInvocation1NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var outerInvocation1InnerInvocation1NestedMethodId = outerInvocation1InnerInvocation1Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
		var outerInvocation1InnerInvocation2Id = outerInvocation1NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation1InnerInvocation2NestedMethodId = outerInvocation1InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		var outerInvocation2Id = outerContainerTemplateId.append(ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE,
			"#2");
		var outerInvocation2NestedContainerTemplateId = outerInvocation2Id.append(
			ContainerTemplateTestDescriptor.SEGMENT_TYPE, "NestedTestCase");
		var outerInvocation2InnerInvocation1Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");
		var outerInvocation2InnerInvocation1NestedMethodId = outerInvocation2InnerInvocation1Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
		var outerInvocation2InnerInvocation2Id = outerInvocation2NestedContainerTemplateId.append(
			ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#2");
		var outerInvocation2InnerInvocation2NestedMethodId = outerInvocation2InnerInvocation2Id.append(
			TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

		var results = executeTestsForClass(TwoTimesTwoInvocationsTestCase.class);

		results.allEvents().debug().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(uniqueId(outerContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1Id)), displayName("[1] A")), //
			event(container(uniqueId(outerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation1Id))), //
			event(container(uniqueId(outerInvocation1InnerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation1NestedMethodId))), //
			event(test(uniqueId(outerInvocation1InnerInvocation1NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation1InnerInvocation1NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1InnerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2Id))), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation1InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation1InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation1NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2Id)), displayName("[2] B")), //
			event(container(uniqueId(outerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2NestedContainerTemplateId))), //
			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), started()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation1Id))), //
			event(container(uniqueId(outerInvocation2InnerInvocation1Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation1NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation1NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation1NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation1Id)), finishedSuccessfully()), //

			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2Id))), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), started()), //
			event(dynamicTestRegistered(uniqueId(outerInvocation2InnerInvocation2NestedMethodId))), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), started()), //
			event(test(uniqueId(outerInvocation2InnerInvocation2NestedMethodId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2InnerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerInvocation2NestedContainerTemplateId)), finishedSuccessfully()), //
			event(container(uniqueId(outerInvocation2Id)), finishedSuccessfully()), //

			event(container(uniqueId(outerContainerTemplateId)), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	// TODO #871 Consider moving to EventConditions
	private static Condition<Event> uniqueId(UniqueId uniqueId) {
		return new Condition<>(byTestDescriptor(where(TestDescriptor::getUniqueId, Predicate.isEqual(uniqueId))),
			"descriptor with uniqueId '%s'", uniqueId);
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class TwoInvocationsTestCase {
		@Test
		void a() {
		}

		@Nested
		class NestedTestCase {
			@Test
			@Tag("nested")
			void b() {
			}
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class NestedContainerTemplateWithTwoInvocationsTestCase {
		@Test
		void a() {
		}

		@Nested
		@ContainerTemplate
		@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
		class NestedTestCase {
			@Test
			void b() {
			}
		}
	}

	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	@ContainerTemplate
	static class TwoTimesTwoInvocationsTestCase {
		@Nested
		@ContainerTemplate
		class NestedTestCase {
			@Test
			void test() {
			}
		}
	}

	static class TwoInvocationsContainerTemplateInvocationContextProvider
			implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
				ExtensionContext context) {
			return Stream.of(new Ctx("A"), new Ctx("B"));
		}

		record Ctx(String displayName) implements ContainerTemplateInvocationContext {
			@Override
			public String getDisplayName(int invocationIndex) {
				var defaultDisplayName = ContainerTemplateInvocationContext.super.getDisplayName(invocationIndex);
				return "%s %s".formatted(defaultDisplayName, displayName);
			}
		}
	}
}
