/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

public class TwoTestFactoryExtension implements TestFactoryExtension {

	private static final List<DynamicTest> TWO_TESTS = Arrays.asList(
		dynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
		dynamicTest("failingTest", () -> fail("failing")));

	@Override
	public Stream<DynamicTest> createForContainer(ContainerExtensionContext context) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
		return TWO_TESTS.stream();
	}

}
