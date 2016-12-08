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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CollectionUtils}.
 *
 * @since 1.0
 */
class CollectionUtilsTests {

	@Test
	void getOnlyElementWithNullCollection() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(null);
		});
		assertEquals("collection must not be null", exception.getMessage());
	}

	@Test
	void getOnlyElementWithEmptyCollection() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(emptySet());
		});
		assertEquals("collection must contain exactly one element: []", exception.getMessage());
	}

	@Test
	void getOnlyElementWithSingleElementCollection() {
		Object expected = new Object();
		Object actual = CollectionUtils.getOnlyElement(singleton(expected));
		assertSame(expected, actual);
	}

	@Test
	void getOnlyElementWithMultiElementCollection() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(asList("foo", "bar"));
		});
		assertEquals("collection must contain exactly one element: [foo, bar]", exception.getMessage());
	}

	@Test
	void toListWithNullIterable() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.toList(null);
		});
		assertEquals("iterable must not be null", exception.getMessage());
	}

	@Test
	void toListElementWithNoIterableElement() {
		List<?> list = CollectionUtils.toList(Collections.emptySet());
		assertTrue(list.isEmpty());
	}

	@Test
	void toListElementWithSingleIterableElement() {
		List<String> list = CollectionUtils.toList(singleton("1"));
		assertFalse(list.isEmpty());
		assertEquals("1", list.get(0));
	}

	@Test
	void toListElementWithMultipleIterableElement() {
		List<String> list = CollectionUtils.toList(Arrays.asList("1", "2", "3"));
		assertFalse(list.isEmpty());
		assertEquals("1", list.get(0));
		assertEquals("2", list.get(1));
		assertEquals("3", list.get(2));
	}

}
