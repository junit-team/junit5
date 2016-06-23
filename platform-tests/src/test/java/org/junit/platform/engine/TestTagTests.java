/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class TestTagTests {

	@Test
	void tagEqualsOtherTagsWithSameName() {
		assertEquals(TestTag.create("fast"), TestTag.create("fast"));
		assertEquals(TestTag.create("fast").hashCode(), TestTag.create("fast").hashCode());
		assertNotEquals(null, TestTag.create("fast"));
		assertNotEquals(TestTag.create("fast"), null);
	}

	@Test
	void toStringPrintsName() {
		assertEquals("fast", TestTag.create("fast").toString());
	}

}
