/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.expectThrows;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DynamicTestTests {

	private final List<String> assertedValues = new LinkedList<>();

	@Test
	void streamFromIterator() throws Throwable {
		Stream<DynamicTest> stream = DynamicTest.stream(Arrays.asList("foo", "bar", "baz").iterator(),
			String::toUpperCase, this::throwingConsumer);
		List<DynamicTest> dynamicTests = stream.collect(Collectors.toList());

		assertThat(dynamicTests).hasSize(3).extracting(DynamicTest::getDisplayName).containsExactly("FOO", "BAR",
			"BAZ");

		assertThat(assertedValues).isEmpty();

		dynamicTests.get(0).getExecutable().execute();
		assertThat(assertedValues).containsExactly("foo");

		dynamicTests.get(1).getExecutable().execute();
		assertThat(assertedValues).containsExactly("foo", "bar");

		Throwable t = expectThrows(Throwable.class, () -> dynamicTests.get(2).getExecutable().execute());
		assertThat(t).hasMessage("Baz!");
		assertThat(assertedValues).containsExactly("foo", "bar");
	}

	private void throwingConsumer(String str) throws Throwable {
		if ("baz".equals(str)) {
			throw new Throwable("Baz!");
		}
		this.assertedValues.add(str);
	}

}
