/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

/**
 * Integration test that verifies that the testable annotation may be
 * attached to any element type.
 *
 * @since 5.7
 */
class TestableAnnotationTests {

	@Test
	void testAndRepeatedTest() {
		assertNotNull(new TestableEverywhere().toString());
	}

	@Testable
	static class TestableEverywhere {

		@Testable
		final int field = 0;

		@Testable
		TestableEverywhere() {
		}

		@Testable
		void test(@Testable int parameter) {
			@Testable
			var var = "var";
		}
	}

}
