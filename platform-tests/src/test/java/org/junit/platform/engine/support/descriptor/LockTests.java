/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.jupiter.api.parallel.ResourceLocksProvider.Lock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.platform.AbstractEqualsAndHashCodeTests;

/**
 * @since 5.12
 */
class LockTests extends AbstractEqualsAndHashCodeTests {

	@Test
	void equalsAndHashCode() {
		assertEqualsAndHashCode(new Lock("a"), new Lock("a"), new Lock("b"));
		// @formatter:off
		assertEqualsAndHashCode(
				new Lock("a", ResourceAccessMode.READ_WRITE),
				new Lock("a", ResourceAccessMode.READ_WRITE),
				new Lock("a", ResourceAccessMode.READ)
		);
		// @formatter:on
	}
}
