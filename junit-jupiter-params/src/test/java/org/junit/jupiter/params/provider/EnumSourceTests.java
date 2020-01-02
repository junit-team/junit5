/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCH_ALL;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCH_ANY;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.BAR;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.BAZ;
import static org.junit.jupiter.params.provider.EnumSourceTests.EnumWithThreeConstants.FOO;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class EnumSourceTests {

	@Test
	void includeNamesWithAll() {
		assertAll("include names with all", //
			() -> assertTrue(INCLUDE.select(FOO, allOf(EnumWithThreeConstants::name))),
			() -> assertTrue(INCLUDE.select(BAR, allOf(EnumWithThreeConstants::name))),
			() -> assertTrue(INCLUDE.select(BAZ, allOf(EnumWithThreeConstants::name))));
	}

	@Test
	void includeNamesWithSingleton() {
		assertAll("include names with singleton", //
			() -> assertTrue(INCLUDE.select(FOO, FOO.singleton())),
			() -> assertTrue(INCLUDE.select(BAR, BAR.singleton())),
			() -> assertTrue(INCLUDE.select(BAZ, BAZ.singleton())));
		assertAll("include names with singleton complement", //
			() -> assertFalse(INCLUDE.select(BAR, FOO.singleton())),
			() -> assertFalse(INCLUDE.select(BAZ, FOO.singleton())));
	}

	@Test
	void excludeNames() {
		assertAll("exclude name with none excluded", //
			() -> assertTrue(EXCLUDE.select(FOO, Collections.emptySet())),
			() -> assertTrue(EXCLUDE.select(BAR, Collections.emptySet())),
			() -> assertTrue(EXCLUDE.select(BAZ, Collections.emptySet())));
		assertAll("exclude name with FOO excluded", //
			() -> assertFalse(EXCLUDE.select(FOO, FOO.singleton())),
			() -> assertTrue(EXCLUDE.select(BAR, FOO.singleton())),
			() -> assertTrue(EXCLUDE.select(BAZ, FOO.singleton())));
	}

	@Test
	void matchesAll() {
		assertAll("matches all", //
			() -> assertTrue(MATCH_ALL.select(FOO, Collections.singleton("F.."))),
			() -> assertTrue(MATCH_ALL.select(BAR, Collections.singleton("B.."))),
			() -> assertTrue(MATCH_ALL.select(BAZ, Collections.singleton("B.."))));
		assertAll("matches all fails if not all match", //
			() -> assertFalse(MATCH_ALL.select(FOO, set("F..", "."))),
			() -> assertFalse(MATCH_ALL.select(BAR, set("B..", "."))),
			() -> assertFalse(MATCH_ALL.select(BAZ, set("B..", "."))));
	}

	@Test
	void matchesAny() {
		assertAll("matches any", //
			() -> assertTrue(MATCH_ANY.select(FOO, set("B..", "^F.*"))),
			() -> assertTrue(MATCH_ANY.select(BAR, set("B", "B.", "B.."))),
			() -> assertTrue(MATCH_ANY.select(BAZ, set("^.+[zZ]$"))));
	}

	enum EnumWithThreeConstants {
		FOO, BAR, BAZ;

		Set<String> singleton() {
			return Collections.singleton(name());
		}
	}

	static Set<String> allOf(Function<EnumWithThreeConstants, String> mapper) {
		return EnumSet.allOf(EnumWithThreeConstants.class).stream().map(mapper).collect(toSet());
	}

	static Set<String> set(String... strings) {
		return new HashSet<>(Arrays.asList(strings));
	}

}
