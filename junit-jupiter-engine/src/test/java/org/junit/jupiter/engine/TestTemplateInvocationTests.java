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

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

public class TestTemplateInvocationTests extends AbstractJupiterTestEngineTests {

	@Test
	void templateWithoutExtensionReportsError() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyTestTemplateTestCase.class, "templateWithoutRegisteredExtension")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyTestTemplateTestCase.class), started()), //
			event(container("templateWithoutRegisteredExtension"), started()), //
			event(container("templateWithoutRegisteredExtension"),
				finishedWithFailure(message(
					"You need to register at least one TestTemplateInvocationContextProvider for this method"))), //
			event(container(MyTestTemplateTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	private static class MyTestTemplateTestCase {

		@TestTemplate
		void templateWithoutRegisteredExtension() {
		}

	}
}
