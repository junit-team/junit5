/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModuleUtils}.
 *
 * @since 1.1
 */
class ModuleUtilsTests {

	@Test
	void isJavaPlatformModuleSystemAvailable() {
		boolean expected;
		try {
			Class.forName("java.lang.Module");
			expected = true;
		}
		catch (ClassNotFoundException e) {
			expected = false;
		}
		assertEquals(expected, ModuleUtils.isJavaPlatformModuleSystemAvailable());
	}

}
