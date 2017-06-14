/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.injection.sample.PrimitiveArrayParameterResolver;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Unit tests for UniqueId.parse for methods with array type parameters.
 *
 * @see <a href="https://github.com/junit-team/junit5/issues/810">#810</a>
 *
 * @since 5.0
 */
class ArrayTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeTestsForPrimitiveArrayMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PrimitiveArrayMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		eventRecorder.getExecutionEvents().stream().map(ExecutionEvent::getTestDescriptor).distinct().skip(2).map(
			TestDescriptor::getUniqueId).forEach(id -> UniqueId.parse(id.toString()));
	}

	@ExtendWith(PrimitiveArrayParameterResolver.class)
	private static class PrimitiveArrayMethodInjectionTestCase {

		@Test
		void primitiveArray(int... ints) {
			assertArrayEquals(new int[] { 1, 2, 3 }, ints);
		}
	}

}
