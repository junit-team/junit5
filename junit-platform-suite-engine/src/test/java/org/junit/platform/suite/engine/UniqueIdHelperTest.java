/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.UniqueId;

class UniqueIdHelperTest {

	@Test
	void append() {
		UniqueId uniqueId = UniqueId.root("a", "value").append("b", "value");
		UniqueId suffix = UniqueId.root("c", "value").append("d", "value");
		UniqueId expected = uniqueId.append("c", "value").append("d", "value");
		assertEquals(expected, UniqueIdHelper.append(uniqueId, suffix));
	}

	@Test
	void uniqueIdOfSegment() {
		UniqueId root = UniqueId.root("a", "value");
		UniqueId uniqueId = root.append("b", "value");
		// @formatter:off
		assertAll(
				() -> assertEquals(Optional.of(uniqueId), UniqueIdHelper.uniqueIdOfSegment(uniqueId, "b")),
				() -> assertEquals(Optional.of(root), UniqueIdHelper.uniqueIdOfSegment(uniqueId, "a")),
				() -> assertEquals(Optional.empty(), UniqueIdHelper.uniqueIdOfSegment(uniqueId, "c"))
		);
		// @formatter:on
	}

	@Test
	void removePrefix() {
		UniqueId prefix = UniqueId.root("a", "value").append("b", "value");
		UniqueId uniqueId = prefix.append("c", "value").append("d", "value");
		UniqueId expected = UniqueId.root("c", "value").append("d", "value");
		// @formatter:off
		assertAll(
				() -> assertEquals(Optional.empty(), UniqueIdHelper.removePrefix(uniqueId, uniqueId)),
				() -> assertEquals(Optional.of(expected), UniqueIdHelper.removePrefix(uniqueId, prefix)),
				() -> assertThrows(PreconditionViolationException.class,
				() -> UniqueIdHelper.removePrefix(prefix, uniqueId))
		);
		// @formatter:on
	}

	@Test
	void containsCycle() {
		// @formatter:off
		UniqueId uniqueId = UniqueId
				.root("segment-one", "a")
				.append("segment-two", "b")
				.append("segment-one", "c")
				.append("segment-two", "b");
		// @formatter:on

		// @formatter:off
		assertAll(
				() -> assertFalse(UniqueIdHelper.containCycle(uniqueId, "segment-one")),
				() -> assertTrue(UniqueIdHelper.containCycle(uniqueId, "segment-two"))
		);
		// @formatter:off

	}

}
