/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.gen5.api.Test;

public class UniqueIdTests {
	@Test
	void givenNull_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from(null);
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenEmptyString_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from("");
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenNulls_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from(null, null);
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenNullKey_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from(null, "value");
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenNullValue_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from("key", null);
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenEmptyStrings_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from("", "");
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenEmptyKey_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from("", "value");
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenEmptyValue_returnsAnEmptyUniqueId() {
		UniqueId uniqueId = UniqueId.from("key", "");
		assertUniqueIdIsEmpty(uniqueId);
	}

	@Test
	void givenASingleSegment_returnsUniqueIdWithoutRemainder() {
		UniqueId uniqueId = UniqueId.from("key", "value");
		assertThat(uniqueId.isEmpty()).isFalse();
		assertThat(uniqueId.numberOfSegments()).isEqualTo(1);
		assertThat(uniqueId.currentKey()).isEqualTo("key");
		assertThat(uniqueId.currentValue()).isEqualTo("value");
		assertThat(uniqueId.toString()).isEqualTo("[key:value]");

		assertUniqueIdIsEmpty(uniqueId.getRemainder());
	}

	@Test
	void givenTwoSegments_returnsUniqueIdWithRemainder() {
		UniqueId uniqueId = UniqueId.from("first", "one").append("second", "two");
		assertThat(uniqueId.isEmpty()).isFalse();
		assertThat(uniqueId.numberOfSegments()).isEqualTo(2);
		assertThat(uniqueId.currentKey()).isEqualTo("first");
		assertThat(uniqueId.currentValue()).isEqualTo("one");
		assertThat(uniqueId.toString()).isEqualTo("[first:one]/[second:two]");

		UniqueId remainder = uniqueId.getRemainder();
		assertThat(remainder.isEmpty()).isFalse();
		assertThat(remainder.numberOfSegments()).isEqualTo(1);
		assertThat(remainder.currentKey()).isEqualTo("second");
		assertThat(remainder.currentValue()).isEqualTo("two");
		assertThat(remainder.toString()).isEqualTo("[second:two]");

		assertUniqueIdIsEmpty(remainder.getRemainder());
	}

	@Test
	void givenALongUniqueIdString_returnsCorrectUniqueIdObject() {
		UniqueId uniqueId = UniqueId.from("[first:one]/[second:two]");

		assertThat(uniqueId.isEmpty()).isFalse();
		assertThat(uniqueId.numberOfSegments()).isEqualTo(2);
		assertThat(uniqueId.currentKey()).isEqualTo("first");
		assertThat(uniqueId.currentValue()).isEqualTo("one");
		assertThat(uniqueId.toString()).isEqualTo("[first:one]/[second:two]");

		UniqueId remainder = uniqueId.getRemainder();
		assertThat(remainder.isEmpty()).isFalse();
		assertThat(remainder.numberOfSegments()).isEqualTo(1);
		assertThat(remainder.currentKey()).isEqualTo("second");
		assertThat(remainder.currentValue()).isEqualTo("two");
		assertThat(remainder.toString()).isEqualTo("[second:two]");

		assertUniqueIdIsEmpty(remainder.getRemainder());
	}

	@Test
	void appendingEmptyOntoEmpty_remainsEmpty() {
		UniqueId empty1 = UniqueId.empty();
		UniqueId empty2 = UniqueId.empty();
		UniqueId combined = empty1.append(empty2);
		assertUniqueIdIsEmpty(combined);
	}

	private void assertUniqueIdIsEmpty(UniqueId uniqueId) {
		assertThat(uniqueId.isEmpty()).isTrue();
		assertThat(uniqueId.numberOfSegments()).isEqualTo(0);
		assertThat(uniqueId.currentKey()).isEmpty();
		assertThat(uniqueId.currentValue()).isEmpty();
		assertThat(uniqueId.getRemainder().isEmpty()).isTrue();
		assertThat(uniqueId.toString()).isEmpty();
	}
}
