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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Unit tests for {@link org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource}
 * stored values.
 *
 * @since 1.1
 */
@ExtendWith(Heavyweight.class)
@ResourceLock(Heavyweight.Resource.ID)
class HeavyweightAlphaTests {

	private static int mark;

	@BeforeAll
	static void setMark(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 0);
		mark = resource.usages();
	}

	@TestFactory
	Stream<DynamicTest> alpha1(Heavyweight.Resource resource) {
		return Stream.of(dynamicTest("foo", () -> assertTrue(resource.usages() > 1)));
	}

	@Test
	void alpha2(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 1);
	}

	@Test
	void alpha3(Heavyweight.Resource resource) {
		assertTrue(resource.usages() > 1);
	}

	@AfterAll
	static void checkMark(Heavyweight.Resource resource) {
		assertEquals(mark, resource.usages() - 4);
	}
}
