/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class NonVoidTestableMethodIntegrationTests {

	@Test
	void valid() {
	}

	@Test
	int invalidMethodReturningPrimitive() {
		fail("This method should never have been called.");
		return 1;
	}

	@Test
	String invalidMethodReturningObject() {
		fail("This method should never have been called.");
		return "";
	}

	@RepeatedTest(3)
	int invalidMethodVerifyingTestTemplateMethod() {
		fail("This method should never have been called.");
		return 1;
	}

}
