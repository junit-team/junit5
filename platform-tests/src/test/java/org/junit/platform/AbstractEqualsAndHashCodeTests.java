/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for unit tests that wish to test
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 *
 * @since 1.3
 */
public abstract class AbstractEqualsAndHashCodeTests {

	protected final <T> void assertEqualsAndHashCode(T equal1, T equal2, T different) {
		assertNotNull(equal1);
		assertNotNull(equal2);
		assertNotNull(different);

		assertNotSame(equal1, equal2);
		assertFalse(equal1.equals(null));
		assertFalse(equal1.equals(new Object()));
		assertFalse(equal1.equals(different));
		assertFalse(different.equals(equal1));
		assertNotEquals(equal1.hashCode(), different.hashCode());

		assertTrue(equal1.equals(equal1));
		assertTrue(equal1.equals(equal2));
		assertTrue(equal2.equals(equal1));
		assertEquals(equal1.hashCode(), equal2.hashCode());
	}

}
