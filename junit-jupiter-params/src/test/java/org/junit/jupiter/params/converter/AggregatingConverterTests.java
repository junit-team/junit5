/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AggregatingConverterTests {

	@ParameterizedTest
	@CsvSource({ "1, 2", "3, 4" })
	void injectedCollectionsAreValid(@TestData List<String> l, @TestData Map<String, String> map,
			@TestData HashSet<String> set) {
		assertNotNull(l);
		assertNotNull(map);
		assertNotNull(set);

		assertEquals(l.size(), 2);
		assertEquals(map.size(), 2);
		assertEquals(set.size(), 2);
		assertAll("List validity", () -> assertTrue(l.get(0).equals("1") || l.get(0).equals("3")),
			() -> assertTrue(l.get(1).equals("2") || l.get(1).equals("4")));

	}
}
