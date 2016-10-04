/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

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
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Preconditions}.
 *
 * @since 1.0
 */
class PreconditionsTests {

	@Test
	void notNullThrowsForNullObject() {
		String message = "argument is null";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull((Object) null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notNullPassesForNonNullObject() {
		Object object = new Object();
		Object nonNullObject = notNull(object, "");
		assertSame(object, nonNullObject);
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
	void notNullForNullObjectArray() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull((Object[]) null, ""));

		assertEquals("Object array must not be null", exception.getMessage());
	}

	@Test
	void notNullForNullObjectArrayWithCustomMessage() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull((Object[]) null, "custom message"));

		assertEquals("custom message", exception.getMessage());
	}

	@Test
	void notNullPassesForEmptyObjectArray() {
		Object[] objects = new Object[0];
		Object nonNullObjects = notNull(objects, "");
		assertSame(objects, nonNullObjects);
	}

	@Test
	void notNullPassesForObjectArrayContainingObjects() {
		Object[] objects = { new Object(), new Object(), new Object() };
		Object nonNullObjects = notNull(objects, "");
		assertSame(objects, nonNullObjects);
	}

	@Test
	void notNullThrowsForObjectArrayContainingNulls() {
		String message = "there is null in the array";
		Object[] objects = { new Object(), null, new Object() };

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notNull(objects, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyPassesForNonEmptyCollection() {
		Collection<String> collection = Arrays.asList("a", "b", "c");
		Collection<String> nonEmptyCollection = notEmpty(collection, "");
		assertSame(collection, nonEmptyCollection);
	}

	@Test
	void notEmptyThrowsForNullCollection() {
		String message = "collection is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty(null, message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyThrowsForEmptyCollection() {
		String message = "collection is empty";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty(Collections.emptyList(), message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notEmptyThrowsForCollectionWithNullElements() {
		String message = "collection contains null elements";

		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> notEmpty(Collections.singletonList(null), message));

		assertEquals(message, exception.getMessage());
	}

	@Test
	void notBlankPassesForNonBlankString() {
		String string = "abc";
		String nonBlankString = notBlank(string, "");
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
