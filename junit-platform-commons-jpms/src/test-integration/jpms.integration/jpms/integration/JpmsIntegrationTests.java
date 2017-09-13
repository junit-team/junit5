/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package jpms.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class JpmsIntegrationTests {

	@TestFactory
	DynamicNode[] greetings() {
		return new DynamicNode[] { DynamicTest.dynamicTest("hello", () -> {
		}) };
	}

	@Test
	void insideExplicitModule() {
		assertEquals("jpms.integration", getClass().getModule().getName());
	}

}
