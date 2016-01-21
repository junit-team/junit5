/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.gen5.engine.ExecutionEventConditions.*;
import static org.junit.gen5.engine.TestExecutionResultConditions.*;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.IOException;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.junit5.AbstractJUnit5TestEngineTests;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link InstancePostProcessor}.
 */
public class ExceptionHandlerTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void exceptionHandlerRethrowsException() {
		TestDiscoveryRequest request = request().select(forMethod(MyTestCase.class, "testRethrow")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyTestCase.class), started()), //
			event(test("testRethrow"), started()), //
			event(test("testRethrow"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(container(MyTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	// -------------------------------------------------------------------

	private static class MyTestCase {

		@Test
		@ExtendWith({ RethrowException.class })
		void testRethrow() throws IOException {
			throw new IOException("checked");
		}

	}

	private static class RethrowException implements ExtensionPoint {

	}

}
