/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Unit tests for {@link TestPlanScannerFilter}.
 *
 * @since 1.0
 */
public class TestPlanScannerFilterTests {

	@Test
	void accept() {
		TestPlanScannerFilter filter = new TestPlanScannerFilter(LauncherFactory.create());
		assertTrue(filter.accept(TestCaseWithTestMethod.class), "accepts class with @Test method");
		assertTrue(filter.accept(TestCaseWithTestFactoryMethod.class), "accepts class with @TestFactory method");
	}

	private static class TestCaseWithTestMethod {

		@Test
		void test() {
		}
	}

	private static class TestCaseWithTestFactoryMethod {

		@TestFactory
		Stream<DynamicTest> testFactory() {
			return Stream.empty();
		}
	}

}
