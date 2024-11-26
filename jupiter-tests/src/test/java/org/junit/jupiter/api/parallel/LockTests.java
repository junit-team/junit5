/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.ResourceLocksProvider.Lock;

import org.junit.jupiter.api.AbstractEqualsAndHashCodeTests;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Lock}.
 *
 * @since 5.12
 */
class LockTests extends AbstractEqualsAndHashCodeTests {

	@Test
	void readWriteModeSetByDefault() {
		assertEquals(READ_WRITE, new Lock("a").getAccessMode());
	}

	@Test
	void equalsAndHashCode() {
		// @formatter:off
		assertEqualsAndHashCode(
				new Lock("a", READ_WRITE),
				new Lock("a", READ_WRITE),
				new Lock("b", READ_WRITE)
		);
		assertEqualsAndHashCode(
				new Lock("a", READ_WRITE),
				new Lock("a", READ_WRITE),
				new Lock("a", READ)
		);
		// @formatter:on
	}
}
