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

import static org.junit.Assert.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotEquals;

import org.junit.gen5.api.Test;

/**
 * @since 5.0
 */
class TestTagTests {

	@Test
	void tagEqualsOtherTagsWithSameName() {
		assertEquals(new TestTag("fast"), new TestTag("fast"));
		assertEquals(new TestTag("fast").hashCode(), new TestTag("fast").hashCode());
		assertNotEquals(null, new TestTag("fast"));
		assertNotEquals(new TestTag("fast"), null);
	}

	@Test
	void toStringPrintsName() {
		assertEquals("fast", new TestTag("fast").toString());
	}

}
