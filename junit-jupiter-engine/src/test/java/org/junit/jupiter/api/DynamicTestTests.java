/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opentest4j.AssertionFailedError;

class DynamicTestTests {

	private final List<String> assertedValues = new ArrayList<>();

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

		Throwable t = assertThrows(Throwable.class, () -> dynamicTests.get(2).getExecutable().execute());
		assertThat(t).hasMessage("Baz!");
		assertThat(assertedValues).containsExactly("foo", "bar");
	}

	private void throwingConsumer(String str) throws Throwable {
		if ("baz".equals(str)) {
			throw new Throwable("Baz!");
		}
		this.assertedValues.add(str);
	}

	@Test
	void reflectiveOperationThrowingAssertionFailedError() {
		Throwable t48 = assertThrows(AssertionFailedError.class,
			() -> dynamicTest("1 == 48", this::assertOneEquals48).getExecutable().execute());
		assertThat(t48).hasMessage("expected: <1> but was: <48>");

		Throwable t49 = assertThrows(AssertionFailedError.class,
			() -> dynamicTest("1 == 49", this::assertOnEquals49).getExecutable().execute());
		assertThat(t49).hasMessage("expected: <1> but was: <49>");
	}

	@Test
	@Disabled("https://github.com/junit-team/junit5/issues/1322")
	void reflectiveOperationThrowingUnexpectedException() {
		// Fails with: Unexpected exception type thrown
		//   ==> expected: <org.opentest4j.AssertionFailedError>
		//        but was: <java.lang.reflect.InvocationTargetException>
		Throwable t50 = assertThrows(AssertionFailedError.class,
			() -> dynamicTest("1 == 50", this::assertOneEquals50).getExecutable().execute());
		assertThat(t50).hasMessage("expected: <1> but was: <50>");
	}

	private void assertOneEquals48() {
		Assertions.assertEquals(1, 48);
	}

	private void assertOnEquals49() throws Throwable {
		Method method = Assertions.class.getMethod("assertEquals", int.class, int.class);
		try {
			method.invoke(null, 1, 49);
		}
		catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private void assertOneEquals50() throws Throwable {
		Method method = Assertions.class.getMethod("assertEquals", int.class, int.class);
		method.invoke(null, 1, 50);
	}

}
