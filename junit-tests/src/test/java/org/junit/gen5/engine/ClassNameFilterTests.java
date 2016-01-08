/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.*;

import java.util.Collection;

import org.junit.gen5.api.Test;

class ClassNameFilterTests {

	@Test
	void filtersByClassName() {
		String regex = "^java\\.lang\\..*";
		ClassFilter filter = new ClassNameFilter(regex);
		assertEquals("Filter class names with regular expression: " + regex, filter.getDescription());
		assertTrue(filter.acceptClass(String.class));
		assertFalse(filter.acceptClass(Collection.class));
	}

}
