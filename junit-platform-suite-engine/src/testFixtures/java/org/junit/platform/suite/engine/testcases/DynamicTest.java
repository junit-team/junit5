/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine.testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.TestFactory;

public class DynamicTest {

	@TestFactory
	Collection<org.junit.jupiter.api.DynamicTest> dynamicTestsWithCollection() {
		return Arrays.asList(
			org.junit.jupiter.api.DynamicTest.dynamicTest("Add test", () -> assertEquals(2, Math.addExact(1, 1))),
			org.junit.jupiter.api.DynamicTest.dynamicTest("Multiply Test",
				() -> assertEquals(4, Math.multiplyExact(2, 2))));
	}

}
