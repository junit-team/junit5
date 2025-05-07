/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;

/**
 * @since 5.13
 */
class BooleansSourceTests {

	@ParameterizedTest
	@BooleansSource
	void shouldRunWithTrueAndFalse(boolean flag) {
		assertTrue(flag || !flag);
	}
	
}
