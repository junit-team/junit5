/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModuleUtils}.
 *
 * @since 1.1
 */
class ModuleUtilsTests {

	@Test
	void loadsTestFinder() {
		List<Class<?>> classes = ModuleUtils.findAllClassesInModule("*", __ -> true, __ -> true);
		assertTrue(classes.size() > 0);
		assertEquals(1, classes.stream().filter(type -> type == ModuleTestFinder.class).count());
	}

}
