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

import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.uniqueIdSubstring;

import java.util.stream.Stream;

import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @since 5.13
 */
public class ContainerTemplateInvocationTests extends AbstractJupiterTestEngineTests {

	//	@Disabled("not yet implemented")
	@Test
	void executesContainerTemplateClassTwice() {
		var results = executeTestsForClass(TwoInvocationsTestCase.class);
		results.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(TwoInvocationsTestCase.class), uniqueIdSubstring("container-template"), started()), //
			event(dynamicTestRegistered("container-template-invocation:#1"), displayName("[1] A")), //
			event(container("container-template-invocation:#1"), started()), //
			event(dynamicTestRegistered("a")), //
			event(dynamicTestRegistered("b")), //
			event(test("a"), started()), //
			event(test("a"), finishedSuccessfully()), //
			event(test("b"), started()), //
			event(test("b"), finishedSuccessfully()), //
			event(container("container-template-invocation:#1"), finishedSuccessfully()), //
			event(dynamicTestRegistered("container-template-invocation:#2"), displayName("[2] B")), //
			event(container("container-template-invocation:#2"), started()), //
			event(dynamicTestRegistered("a")), //
			event(dynamicTestRegistered("b")), //
			event(test("a"), started()), //
			event(test("a"), finishedSuccessfully()), //
			event(test("b"), started()), //
			event(test("b"), finishedSuccessfully()), //
			event(container("container-template-invocation:#2"), finishedSuccessfully()), //
			event(container(TwoInvocationsTestCase.class), uniqueIdSubstring("container-template"),
				finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ContainerTemplate
	@ExtendWith(TwoInvocationsContainerTemplateInvocationContextProvider.class)
	static class TwoInvocationsTestCase {
		@Test
		void a() {
		}

		@Test
		void b() {
		}
	}

	static class TwoInvocationsContainerTemplateInvocationContextProvider
			implements ContainerTemplateInvocationContextProvider {

		@Override
		public boolean supportsContainerTemplate(ExtensionContext context) {
			return TwoInvocationsTestCase.class.equals(context.getRequiredTestClass());
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
