/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.jupiter.api.extension.TwoTestFactoryExtension;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Integration tests for {@link TestFactoryExtension TestFactoryExtension}s.
 *
 * @since 5.0
 */
class TestFactoryExtensionMethodTests extends AbstractJupiterTestEngineTests {

	@Test
	void testFactoryExtensionMethodsAreCorrectlyDiscoveredForClassSelector() {
		LauncherDiscoveryRequest request = request().selectors(selectJavaClass(MyTestFactoryExtensionTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(3, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void testFactoryExtensionMethodIsCorrectlyDiscoveredForMethodSelector() {
		LauncherDiscoveryRequest request = request().selectors(
			selectJavaMethod(MyTestFactoryExtensionTestCase.class, "extendedMethod")).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getAllDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void dynamicTestsCreatedByExtensionAreExecuted() {
		LauncherDiscoveryRequest request = request().selectors(
			selectJavaMethod(MyTestFactoryExtensionTestCase.class, "extendedMethod")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyTestFactoryExtensionTestCase.class), started()), //
			event(container("extendedMethod"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("extendedMethod"), finishedSuccessfully()), //
			event(container(MyTestFactoryExtensionTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	private static class MyTestFactoryExtensionTestCase {

		@ExtendWith(TwoTestFactoryExtension.class)
		void extendedMethod() {
		}

		@ExtendWith(TwoTestFactoryExtension.class)
		void extendedAnotherMethod() {
		}

	}

}
