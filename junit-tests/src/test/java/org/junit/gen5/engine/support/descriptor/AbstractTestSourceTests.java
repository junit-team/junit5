/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertNotEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertNotSame;
import static org.junit.gen5.api.Assertions.assertTrue;

import org.junit.gen5.engine.TestSource;

/**
 * Abstract base class for unit tests involving {@link TestSource TestSources}.
 *
 * @since 5.0
 */
abstract class AbstractTestSourceTests {

	protected void assertEqualsAndHashCode(TestSource equal1, TestSource equal2, TestSource different) {
		assertNotNull(equal1);
		assertNotNull(equal2);
		assertNotNull(different);

		assertNotSame(equal1, equal2);
		assertFalse(equal1.equals(null));
		assertFalse(equal1.equals(different));
		assertFalse(different.equals(equal1));
		assertNotEquals(equal1.hashCode(), different.hashCode());

		assertTrue(equal1.equals(equal1));
		assertTrue(equal1.equals(equal2));
		assertTrue(equal2.equals(equal1));
		assertEquals(equal1.hashCode(), equal2.hashCode());
	}

}
