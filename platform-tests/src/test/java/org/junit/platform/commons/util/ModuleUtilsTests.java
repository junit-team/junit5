/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModuleUtils}.
 *
 * @since 1.1
 */
class ModuleUtilsTests {

	@Test
	void isJavaPlatformModuleSystemAvailable() {
		// when running clover for code coverage the mr-jar is not created
		// which leads to 'false' fails here - so exit here if clover is running
		assumeFalse(Boolean.getBoolean("coverage.enabled"));
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
