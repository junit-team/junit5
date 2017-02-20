/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.bridge.NumberTestGroup.ByteTestCase;
import org.junit.jupiter.engine.bridge.NumberTestGroup.ShortTestCase;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * @since 5.0
 */
class BridgeMethodTests extends AbstractJupiterTestEngineTests {

	static List<String> sequence = new ArrayList<>();

	@Test
	void childrenHaveBridgeMethods() throws Exception {
		assertFalse(ChildWithBridgeMethods.class.getMethod("anotherBeforeEach").isBridge());
		assertFalse(ChildWithBridgeMethods.class.getMethod("anotherAfterEach").isBridge());
		assertTrue(ChildWithBridgeMethods.class.getMethod("beforeEach").isBridge());
		assertTrue(ChildWithBridgeMethods.class.getMethod("afterEach").isBridge());

		assertTrue(ByteTestCase.class.getDeclaredMethod("test", Number.class).isBridge());
		assertFalse(ByteTestCase.class.getDeclaredMethod("test", Byte.class).isBridge());

		assertTrue(ShortTestCase.class.getDeclaredMethod("test", Number.class).isBridge());
		assertFalse(ShortTestCase.class.getDeclaredMethod("test", Short.class).isBridge());
	}

	@Test
	void childHasNoBridgeMethods() throws Exception {
		assertFalse(ChildWithoutBridgeMethods.class.getMethod("anotherBeforeEach").isBridge());
		assertFalse(ChildWithoutBridgeMethods.class.getMethod("anotherAfterEach").isBridge());
		assertFalse(ChildWithoutBridgeMethods.class.getMethod("beforeEach").isBridge());
		assertFalse(ChildWithoutBridgeMethods.class.getMethod("afterEach").isBridge());
	}

	@Test
	void compareMethodExecutionSequenceOrder() {
		String withoutBridgeMethods = execute(ChildWithoutBridgeMethods.class);
		String withBridgeMethods = execute(ChildWithBridgeMethods.class);
		assertEquals(withoutBridgeMethods, withBridgeMethods);
	}

	@TestFactory
	List<DynamicTest> ensureSingleTestMethodsExecute() {
		return Arrays.asList(
			dynamicTest("Byte", //
				() -> assertEquals("[test(Byte) BEGIN, test(N), test(Byte) END.]", //
					execute(ByteTestCase.class))),
			dynamicTest("Short", //
				() -> assertEquals("[test(Short) BEGIN, test(N), test(Short) END.]", //
					execute(ShortTestCase.class))));
	}

	private String execute(Class<?> testClass) {
		sequence.clear();
		ExecutionEventRecorder recorder = executeTestsForClass(testClass);
		assertEquals(1, recorder.getTestFinishedCount());
		return sequence.toString();
	}

}
