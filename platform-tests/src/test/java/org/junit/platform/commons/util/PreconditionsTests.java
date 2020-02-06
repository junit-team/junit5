/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.Preconditions.condition;
import static org.junit.platform.commons.util.Preconditions.containsNoNullElements;
import static org.junit.platform.commons.util.Preconditions.notBlank;
import static org.junit.platform.commons.util.Preconditions.notEmpty;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link Preconditions}.
 *
 * @since 1.0
 */
class PreconditionsTests {

	@Test
	void notNullPassesForNonNullObject() {
		Object object = new Object();
		Object nonNullObject = notNull(object, "message");
		assertSame(object, nonNullObject);
	}

	@Test
	void notNullThrowsForNullObject() {
		String message = "argument is null";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull(null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notNullThrowsForNullObjectAndMessageSupplier() {
		String message = "argument is null";
		Object object = null;

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull(object, () -> message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyPassesForNonEmptyArray() {
		String[] array = new String[] { "a", "b", "c" };
		String[] nonEmptyArray = notEmpty(array, () -> "should not fail");
		assertSame(array, nonEmptyArray);
	}

	@Test
	void notEmptyPassesForNonEmptyCollection() {
		Collection<String> collection = Arrays.asList("a", "b", "c");
		Collection<String> nonEmptyCollection = notEmpty(collection, () -> "should not fail");
		assertSame(collection, nonEmptyCollection);
	}

	@Test
	void notEmptyPassesForArrayWithNullElements() {
		notEmpty(new String[] { null }, "message");
	}

	@Test
	void notEmptyPassesForCollectionWithNullElements() {
		notEmpty(singletonList(null), "message");
	}

	@Test
	void notEmptyThrowsForNullArray() {
		String message = "array is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty((Object[]) null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyThrowsForNullCollection() {
		String message = "collection is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty((Collection<?>) null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyThrowsForEmptyArray() {
		String message = "array is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty(new Object[0], message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyThrowsForEmptyCollection() {
		String message = "collection is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty(emptyList(), message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void containsNoNullElementsPassesForArrayThatIsNullOrEmpty() {
		containsNoNullElements((Object[]) null, "array is null");
		containsNoNullElements((Object[]) null, () -> "array is null");

		containsNoNullElements(new Object[0], "array is empty");
		containsNoNullElements(new Object[0], () -> "array is empty");
	}

	@Test
	void containsNoNullElementsPassesForCollectionThatIsNullOrEmpty() {
		containsNoNullElements((List<?>) null, "collection is null");
		containsNoNullElements(emptyList(), "collection is empty");

		containsNoNullElements((List<?>) null, () -> "collection is null");
		containsNoNullElements(emptyList(), () -> "collection is empty");
	}

	@Test
	void containsNoNullElementsPassesForArrayContainingNonNullElements() {
		String[] input = new String[] { "a", "b", "c" };
		String[] output = containsNoNullElements(input, "message");
		assertSame(input, output);
	}

	@Test
	void containsNoNullElementsPassesForCollectionContainingNonNullElements() {
		Collection<String> input = Arrays.asList("a", "b", "c");
		Collection<String> output = containsNoNullElements(input, "message");
		assertSame(input, output);

		output = containsNoNullElements(input, () -> "message");
		assertSame(input, output);
	}

	@Test
	void containsNoNullElementsThrowsForArrayContainingNullElements() {
		String message = "array contains null elements";
		Object[] array = { new Object(), null, new Object() };

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> containsNoNullElements(array, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void containsNoNullElementsThrowsForCollectionContainingNullElements() {
		String message = "collection contains null elements";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> containsNoNullElements(singletonList(null), message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankPassesForNonBlankString() {
		String string = "abc";
		String nonBlankString = notBlank(string, "message");
		assertSame(string, nonBlankString);
	}

	@Test
	void notBlankThrowsForNullString() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank(null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankThrowsForNullStringWithMessageSupplier() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank(null, () -> message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankThrowsForEmptyString() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank("", message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankThrowsForEmptyStringWithMessageSupplier() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank("", () -> message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankThrowsForBlankString() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank("          ", message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankThrowsForBlankStringWithMessageSupplier() {
		String message = "string shouldn't be blank";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notBlank("          ", () -> message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void conditionPassesForTruePredicate() {
		condition(true, "error message");
	}

	@Test
	void conditionPassesForTruePredicateWithMessageSupplier() {
		condition(true, () -> "error message");
	}

	@Test
	void conditionThrowsForFalsePredicate() {
		String message = "condition does not hold";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> condition(false, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void conditionThrowsForFalsePredicateWithMessageSupplier() {
		String message = "condition does not hold";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> condition(false, () -> message));

		assertEquals(message, exception.getMessage());
	}

}
