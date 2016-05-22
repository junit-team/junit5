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

	protected void assertEqualsAndHashCode(TestSource source1, TestSource source2) {
		assertNotNull(source1);
		assertNotNull(source2);
		assertNotSame(source1, source2);
		assertFalse(source1.equals(null));

		assertTrue(source1.equals(source1));
		assertTrue(source1.equals(source2));
		assertTrue(source2.equals(source1));
		assertEquals(source1.hashCode(), source2.hashCode());
	}

}
