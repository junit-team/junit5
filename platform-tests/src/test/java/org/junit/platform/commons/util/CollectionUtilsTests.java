/*
 * Copyright 2015-2017 the original author or authors.
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
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

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
	void toUnmodifiableListThrowsOnMutation() {
		List<Integer> numbers = Stream.of(1).collect(toUnmodifiableList());
		assertThrows(UnsupportedOperationException.class, () -> numbers.clear());
	}

	@Test
	void toStreamWithNull() {
		Exception exception = assertThrows(PreconditionViolationException.class, () -> CollectionUtils.toStream(null));

		assertThat(exception).hasMessage("Object must not be null");
	}

	@Test
	void toStreamWithUnsupportedObjectType() {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> CollectionUtils.toStream("unknown"));

		assertThat(exception).hasMessage("Cannot convert instance of java.lang.String into a Stream: unknown");
	}

	@Test
	void toStreamWithExistingStream() {
		Stream<String> input = Stream.of("foo");

		Stream<?> result = CollectionUtils.toStream(input);

		assertThat(result).isSameAs(input);
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Test
	void toStreamWithCollection() {
		AtomicBoolean collectionStreamClosed = new AtomicBoolean(false);
		Collection<String> input = new ArrayList<String>() {

			{
				add("foo");
				add("bar");
			}

			@Override
			public Stream<String> stream() {
				return super.stream().onClose(() -> collectionStreamClosed.set(true));
			}
		};

		try (Stream<String> stream = (Stream<String>) CollectionUtils.toStream(input)) {
			List<String> result = stream.collect(toList());
			assertThat(result).containsExactly("foo", "bar");
		}

		assertThat(collectionStreamClosed.get()).describedAs("collectionStreamClosed").isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	void toStreamWithIterable() {

		Iterable<String> input = new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return asList("foo", "bar").iterator();
			}
		};

		Stream<String> result = (Stream<String>) CollectionUtils.toStream(input);

		assertThat(result).containsExactly("foo", "bar");
	}

	@SuppressWarnings("unchecked")
	@Test
	void toStreamWithIterator() {
		Iterator<String> input = asList("foo", "bar").iterator();

		Stream<String> result = (Stream<String>) CollectionUtils.toStream(input);

		assertThat(result).containsExactly("foo", "bar");
	}

	@SuppressWarnings("unchecked")
	@Test
	void toStreamWithArray() {
		Stream<String> result = (Stream<String>) CollectionUtils.toStream(new String[] { "foo", "bar" });

		assertThat(result).containsExactly("foo", "bar");
	}

}
