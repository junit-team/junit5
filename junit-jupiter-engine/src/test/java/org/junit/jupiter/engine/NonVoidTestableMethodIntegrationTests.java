/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class NonVoidTestableMethodIntegrationTests {

	@Test
	void valid() {
	}

	//this method must never be called
	@Test
	int invalidMethodReturningPrimitive() {
		assertTrue(false);
		return 1;
	}

	//this method must never be called
	@Test
	String invalidMethodReturningObject() {
		assertTrue(false);
		return "";
	}

	//this method must never be called
	@RepeatedTest(3)
	int invalidMethodVerifyingTestTemplateMethod() {
		assertTrue(false);
		return 1;
	}

}
