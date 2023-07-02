/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

class StreamUtilsTest {

	@Test
	void throwWhenIteratorNamedMethodDoesNotReturnAnIterator() {
		var o = IteratorProviderNotUsable.of("Test");
		var e = assertThrows(PreconditionViolationException.class, () -> StreamUtils.tryConvertToStreamByReflection(o));

		e.printStackTrace();
		assertEquals(1, e.getCause().getSuppressed().length);
	}

	@SuppressWarnings("unchecked")
	@Test
	void usesSpliteratorToConvertToStream() {
		var object = SpliteratorProvider.of(List.of("this", "is", "a", "test"));

		var result = (Stream<String>) StreamUtils.tryConvertToStreamByReflection(object);

		assertThat(result).containsExactly("this", "is", "a", "test");
	}

	@SuppressWarnings("unchecked")
	@Test
	void usesIteratorExposingMethodToConvertToStream() {
		var object = IteratorExposing.of(List.of("this", "is", "a", "test"));

		var result = (Stream<String>) StreamUtils.tryConvertToStreamByReflection(object);

		assertThat(result).containsExactly("this", "is", "a", "test");
	}

	/**
	 * An interface that has an iterator method but does not return java.util/Iterator as a return type
	 */
	@FunctionalInterface
	private interface IteratorProviderNotUsable {
		@SuppressWarnings("unused")
		Object iterator();

		static IteratorProviderNotUsable of(Object o) {
			return () -> o;
		}
	}

	/**
	 * An object that exposes:
	 * <ol>
	 * <li>a method with name 'iterator' that does not return an iterator</li>
	 * <li>a method with name 'spliterator' that returns a java.util.Spliterator</li>
	 * </ol>
	 * @param <T> The type of the spliterator
	 */
	@FunctionalInterface
	private interface SpliteratorProvider<T> {

		@SuppressWarnings("unused")
		default Object iterator() {
			return null;
		}

		@SuppressWarnings("unused")
		Spliterator<T> spliterator();

		static <T> SpliteratorProvider<T> of(Iterable<T> iterable) {
			return iterable::spliterator;
		}

	}

	/**
	 * An object that exposes:
	 * <ol>
	 * <li>a method with name 'iterator' that does not return an iterator</li>
	 * <li>a method with name 'spliterator' that does not return a java.util.Spliterator</li>
	 * <li>a method with other name than 'iterator' returning an iterator</li>
	 * </ol>
	 */
	private interface IteratorExposing<T> {

		@SuppressWarnings("unused")
		default Object iterator() {
			return null;
		}

		@SuppressWarnings("unused")
		default Object spliterator() {
			return null;
		}

		@SuppressWarnings("unused")
		Iterator<T> returnsAnIterator();

		static <T> IteratorExposing<T> of(Iterable<T> iterable) {
			return iterable::iterator;
		}
	}
}
