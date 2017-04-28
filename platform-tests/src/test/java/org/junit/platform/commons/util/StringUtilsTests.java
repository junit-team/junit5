/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringUtils}.
 *
 * @since 1.0
 */
class StringUtilsTests {

	@Test
	void blankness() {
		assertTrue(StringUtils.isBlank(null));
		assertTrue(StringUtils.isBlank(""));
		assertTrue(StringUtils.isBlank(" \t\n\r"));
		assertTrue(StringUtils.isNotBlank("."));
	}

	@Test
	void nullSafeToString() {
		assertEquals("null", StringUtils.nullSafeToString(null));
		assertEquals("", StringUtils.nullSafeToString(""));
		assertEquals("\t", StringUtils.nullSafeToString("\t"));
		assertEquals("foo", StringUtils.nullSafeToString("foo"));
		assertEquals("3.14", StringUtils.nullSafeToString(Double.valueOf("3.14")));
		assertEquals("[1, 2, 3]", StringUtils.nullSafeToString(new int[] { 1, 2, 3 }));
		assertEquals("[a, b, c]", StringUtils.nullSafeToString(new char[] { 'a', 'b', 'c' }));
		assertEquals("[foo, bar]", StringUtils.nullSafeToString(new String[] { "foo", "bar" }));
		assertEquals("[34, 42]", StringUtils.nullSafeToString(new Integer[] { 34, 42 }));
		assertEquals("[[2, 4], [3, 9]]", StringUtils.nullSafeToString(new Integer[][] { { 2, 4 }, { 3, 9 } }));
	}

}
