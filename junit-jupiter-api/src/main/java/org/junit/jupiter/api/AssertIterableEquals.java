/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.AssertionUtils.formatIndexes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
		assertIterableEquals(expected, actual, indexes, messageOrSupplier, new LinkedHashMap<>());
	}

	private static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, Deque<Integer> indexes,
			Object messageOrSupplier, Map<Pair, Status> investigatedElements) {

		if (expected == actual) {
			return;
		}
		assertIterablesNotNull(expected, actual, indexes, messageOrSupplier);

		Iterator<?> expectedIterator = expected.iterator();
		Iterator<?> actualIterator = actual.iterator();

		int processed = 0;
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			Object expectedElement = expectedIterator.next();
			Object actualElement = actualIterator.next();

			indexes.addLast(processed);

			assertIterableElementsEqual(expectedElement, actualElement, indexes, messageOrSupplier,
				investigatedElements);

			indexes.removeLast();
			processed++;
		}

		assertIteratorsAreEmpty(expectedIterator, actualIterator, processed, indexes, messageOrSupplier);
	}

	private static void assertIterableElementsEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier, Map<Pair, Status> investigatedElements) {

		// If both are equal, we don't need to check recursively.
		if (Objects.equals(expected, actual)) {
			return;
		}

		// If both are iterables, we need to check whether they contain the same elements.
		if (expected instanceof Iterable && actual instanceof Iterable) {

			Pair pair = new Pair(expected, actual);

			// Before comparing their elements, we check whether we have already checked this pair.
			Status status = investigatedElements.get(pair);

			// If we've already determined that both contain the same elements, we don't need to check them again.
			if (status == Status.CONTAIN_SAME_ELEMENTS) {
				return;
			}

			// If the pair is already under investigation, we fail in order to avoid infinite recursion.
			if (status == Status.UNDER_INVESTIGATION) {
				indexes.removeLast();
				failIterablesNotEqual(expected, actual, indexes, messageOrSupplier);
			}

			// Otherwise, we put the pair under investigation and recurse.
			investigatedElements.put(pair, Status.UNDER_INVESTIGATION);

			assertIterableEquals((Iterable<?>) expected, (Iterable<?>) actual, indexes, messageOrSupplier,
				investigatedElements);

			// If we reach this point, we've checked that the two iterables contain the same elements so we store this information
			// in case we come across the same pair again.
			investigatedElements.put(pair, Status.CONTAIN_SAME_ELEMENTS);
		}

		// Otherwise, they are neither equal nor iterables, so we fail.
		else {
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
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("expected iterable was <null>" + formatIndexes(indexes)) //
				.buildAndThrow();
	}

	private static void failActualIterableIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("actual iterable was <null>" + formatIndexes(indexes)) //
				.buildAndThrow();
	}

	private static void assertIteratorsAreEmpty(Iterator<?> expected, Iterator<?> actual, int processed,
			Deque<Integer> indexes, Object messageOrSupplier) {

		if (expected.hasNext() || actual.hasNext()) {
			AtomicInteger expectedCount = new AtomicInteger(processed);
			expected.forEachRemaining(e -> expectedCount.incrementAndGet());

			AtomicInteger actualCount = new AtomicInteger(processed);
			actual.forEachRemaining(e -> actualCount.incrementAndGet());

			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("iterable lengths differ" + formatIndexes(indexes)) //
					.expected(expectedCount.get()) //
					.actual(actualCount.get()) //
					.buildAndThrow();
		}
	}

	private static void failIterablesNotEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("iterable contents differ" + formatIndexes(indexes)) //
				.expected(expected) //
				.actual(actual) //
				.buildAndThrow();
	}

	private final static class Pair {
		private final Object left;
		private final Object right;

		public Pair(Object left, Object right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Pair that = (Pair) o;
			return Objects.equals(this.left, that.left) //
					&& Objects.equals(this.right, that.right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right);
		}
	}

	private enum Status {
		UNDER_INVESTIGATION, CONTAIN_SAME_ELEMENTS
	}

}
