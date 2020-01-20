/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.formatIndexes;
import static org.junit.jupiter.api.AssertionUtils.formatValues;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * {@code AssertIterable} is a collection of utility methods that support asserting
 * Iterable equality in tests.
 *
 * @since 5.0
 */
class AssertIterableEquals {

	private AssertIterableEquals() {
		/* no-op */
	}

	static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual) {
		assertIterableEquals(expected, actual, (String) null);
	}

	static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, String message) {
		assertIterableEquals(expected, actual, new ArrayDeque<>(), message);
	}

	static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, Supplier<String> messageSupplier) {
		assertIterableEquals(expected, actual, new ArrayDeque<>(), messageSupplier);
	}

	private static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertIterablesNotNull(expected, actual, indexes, messageOrSupplier);

		Iterator<?> expectedIterator = expected.iterator();
		Iterator<?> actualIterator = actual.iterator();

		int processed = 0;
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			processed++;
			Object expectedElement = expectedIterator.next();
			Object actualElement = actualIterator.next();

			if (Objects.equals(expectedElement, actualElement)) {
				continue;
			}

			indexes.addLast(processed - 1);
			assertIterableElementsEqual(expectedElement, actualElement, indexes, messageOrSupplier);
			indexes.removeLast();
		}

		assertIteratorsAreEmpty(expectedIterator, actualIterator, processed, indexes, messageOrSupplier);
	}

	private static void assertIterableElementsEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {
		if (expected instanceof Iterable && actual instanceof Iterable) {
			assertIterableEquals((Iterable<?>) expected, (Iterable<?>) actual, indexes, messageOrSupplier);
		}
		else if (!Objects.equals(expected, actual)) {
			assertIterablesNotNull(expected, actual, indexes, messageOrSupplier);
			failIterablesNotEqual(expected, actual, indexes, messageOrSupplier);
		}
	}

	private static void assertIterablesNotNull(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == null) {
			failExpectedIterableIsNull(indexes, messageOrSupplier);
		}
		if (actual == null) {
			failActualIterableIsNull(indexes, messageOrSupplier);
		}
	}

	private static void failExpectedIterableIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		fail(buildPrefix(nullSafeGet(messageOrSupplier)) + "expected iterable was <null>" + formatIndexes(indexes));
	}

	private static void failActualIterableIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		fail(buildPrefix(nullSafeGet(messageOrSupplier)) + "actual iterable was <null>" + formatIndexes(indexes));
	}

	private static void assertIteratorsAreEmpty(Iterator<?> expected, Iterator<?> actual, int processed,
			Deque<Integer> indexes, Object messageOrSupplier) {

		if (expected.hasNext() || actual.hasNext()) {
			AtomicInteger expectedCount = new AtomicInteger(processed);
			expected.forEachRemaining(e -> expectedCount.incrementAndGet());

			AtomicInteger actualCount = new AtomicInteger(processed);
			actual.forEachRemaining(e -> actualCount.incrementAndGet());

			String prefix = buildPrefix(nullSafeGet(messageOrSupplier));
			String message = "iterable lengths differ" + formatIndexes(indexes) + ", expected: <" + expectedCount.get()
					+ "> but was: <" + actualCount.get() + ">";
			fail(prefix + message);
		}
	}

	private static void failIterablesNotEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		String prefix = buildPrefix(nullSafeGet(messageOrSupplier));
		String message = "iterable contents differ" + formatIndexes(indexes) + ", " + formatValues(expected, actual);
		fail(prefix + message);
	}

}
