/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Unit tests for {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource}
 * stored values.
 *
 * @since 1.1
 */
@ExtendWith(Heavyweight.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ResourceLock(Heavyweight.Resource.ID)
class HeavyweightBetaTests {

	private int mark;

	@BeforeAll
	void beforeAll(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 0);
		mark = resource.usages();
	}

	@BeforeEach
	void beforeEach(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 1);
	}

	@Test
	void beta(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 2);
	}

	@AfterAll
	void afterAll(Heavyweight.Resource resource) {
		assertEquals(mark, resource.usages() - 3);
	}
}
