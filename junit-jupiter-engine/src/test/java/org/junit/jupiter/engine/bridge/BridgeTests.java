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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class BridgeTests extends AbstractJupiterTestEngineTests {

	@Test
	void childHasBridgeMethods() throws Exception {
		assertFalse(ChildWithBridges.class.getMethod("anotherBeforeEach").isBridge());
		assertFalse(ChildWithBridges.class.getMethod("anotherAfterEach").isBridge());
		assertTrue(ChildWithBridges.class.getMethod("beforeEach").isBridge());
		assertTrue(ChildWithBridges.class.getMethod("afterEach").isBridge());
	}

	@Test
	void childHasNoBridgeMethods() throws Exception {
		assertFalse(ChildWithoutBridges.class.getMethod("anotherBeforeEach").isBridge());
		assertFalse(ChildWithoutBridges.class.getMethod("anotherAfterEach").isBridge());
		assertFalse(ChildWithoutBridges.class.getMethod("beforeEach").isBridge());
		assertFalse(ChildWithoutBridges.class.getMethod("afterEach").isBridge());
	}

	@Test
	void compareMethodExecutionSequenceOrder() {
		String withoutBridges = execute(ChildWithoutBridges.class);
		String withBridges = execute(ChildWithBridges.class);
		assertEquals(withoutBridges, withBridges);
	}

	private String execute(Class<?> testClass) {
		PackagePrivateParent.bridgeMethodSequence.clear();
		ExecutionEventRecorder recorder = executeTestsForClass(testClass);
		assertEquals(1, recorder.getTestFinishedCount());
		return PackagePrivateParent.bridgeMethodSequence.toString();
	}
}
