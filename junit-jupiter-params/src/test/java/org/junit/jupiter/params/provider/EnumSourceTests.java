/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE_NAMES;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE_NAMES;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCHES_ALL;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCHES_ANY;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.BAR;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.BAZ;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.FOO;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class EnumSourceTests {

	enum EnumWithThreeConstants {
		FOO, BAR, BAZ;

		Set<String> singleton() {
			return Collections.singleton(name());
		}
	}

	static Set<String> allOf(Function<EnumWithThreeConstants, String> mapper) {
		return EnumSet.allOf(EnumWithThreeConstants.class).stream().map(mapper).collect(Collectors.toSet());
	}

	static Set<String> set(String... strings) {
		return new HashSet<>(Arrays.asList(strings));
	}

	@Test
	void includeNamesWithAll() {
		assertAll("include names with all", //
			() -> assertTrue(INCLUDE_NAMES.select(FOO, allOf(EnumWithThreeConstants::name))),
			() -> assertTrue(INCLUDE_NAMES.select(BAR, allOf(EnumWithThreeConstants::name))),
			() -> assertTrue(INCLUDE_NAMES.select(BAZ, allOf(EnumWithThreeConstants::name))));
	}

	@Test
	void includeNamesWithSingleton() {
		assertAll("include names with singleton", //
			() -> assertTrue(INCLUDE_NAMES.select(FOO, FOO.singleton())),
			() -> assertTrue(INCLUDE_NAMES.select(BAR, BAR.singleton())),
			() -> assertTrue(INCLUDE_NAMES.select(BAZ, BAZ.singleton())));
		assertAll("include names with singleton complement", //
			() -> assertFalse(INCLUDE_NAMES.select(BAR, FOO.singleton())),
			() -> assertFalse(INCLUDE_NAMES.select(BAZ, FOO.singleton())));
	}

	@Test
	void excludeNames() {
		assertAll("exclude name with none excluded", //
			() -> assertTrue(EXCLUDE_NAMES.select(FOO, Collections.emptySet())),
			() -> assertTrue(EXCLUDE_NAMES.select(BAR, Collections.emptySet())),
			() -> assertTrue(EXCLUDE_NAMES.select(BAZ, Collections.emptySet())));
		assertAll("exclude name with FOO excluded", //
			() -> assertFalse(EXCLUDE_NAMES.select(FOO, FOO.singleton())),
			() -> assertTrue(EXCLUDE_NAMES.select(BAR, FOO.singleton())),
			() -> assertTrue(EXCLUDE_NAMES.select(BAZ, FOO.singleton())));
	}

	@Test
	void matchesAll() {
		assertAll("matches all", //
			() -> assertTrue(MATCHES_ALL.select(FOO, Collections.singleton("F.."))),
			() -> assertTrue(MATCHES_ALL.select(BAR, Collections.singleton("B.."))),
			() -> assertTrue(MATCHES_ALL.select(BAZ, Collections.singleton("B.."))));
		assertAll("matches all fails if not all match", //
			() -> assertFalse(MATCHES_ALL.select(FOO, set("F..", "."))),
			() -> assertFalse(MATCHES_ALL.select(BAR, set("B..", "."))),
			() -> assertFalse(MATCHES_ALL.select(BAZ, set("B..", "."))));
	}

	@Test
	void matchesAny() {
		assertAll("matches any", //
			() -> assertTrue(MATCHES_ANY.select(FOO, set("B..", "^F.*"))),
			() -> assertTrue(MATCHES_ANY.select(BAR, set("B", "B.", "B.."))),
			() -> assertTrue(MATCHES_ANY.select(BAZ, set(".*[z|Z]"))));
	}
}
