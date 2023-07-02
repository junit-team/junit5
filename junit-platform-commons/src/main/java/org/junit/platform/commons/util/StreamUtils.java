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

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.function.Try;

/**
 * Collection of utilities for working with {@link Stream Streams}.
 *
 * @since 5.10
 */
final class StreamUtils {

	private StreamUtils() {
	}

	static Stream<?> tryConvertToStreamByReflection(Object object) {
		Preconditions.notNull(object, "Object must not be null");
		Class<?> theClass = object.getClass();
		try {
			String name = "iterator";
			Method method = theClass.getMethod(name);
			if (method.getReturnType() == Iterator.class) {
				return stream(() -> tryIteratorToSpliterator(object, method), ORDERED, false);
			}
			else {
				throw new IllegalStateException(
					"Method with name 'iterator' does not return " + Iterator.class.getName());
			}
		}
		catch (NoSuchMethodException | IllegalStateException e) {
			return tryConvertToStreamBySpliterator(object, e);
		}
	}

	private static Stream<?> tryConvertToStreamBySpliterator(Object object, Exception e) {
		try {
			String name = "spliterator";
			Method method = object.getClass().getMethod(name);
			if (method.getReturnType() == Spliterator.class) {
				return stream(() -> tryInvokeSpliterator(object, method), ORDERED, false);
			}
			else {
				throw new IllegalStateException(
					"Method with name '" + name + "' does not return " + Spliterator.class.getName());
			}
		}
		catch (NoSuchMethodException | IllegalStateException ex) {
			ex.addSuppressed(e);
			return tryConvertByIteratorSpliteratorReturnType(object, ex);
		}
	}

	private static Stream<?> tryConvertByIteratorSpliteratorReturnType(Object object, Exception ex) {
		return streamFromSpliteratorSupplier(object)//
				.orElseGet(() -> streamFromIteratorSupplier(object)//
						.orElseThrow(() -> //
						new PreconditionViolationException(//
							"Cannot convert instance of " + object.getClass().getName() + " into a Stream: " + object,
							ex)));
	}

	private static Optional<Stream<?>> streamFromSpliteratorSupplier(Object object) {
		return Arrays.stream(object.getClass().getMethods())//
				.filter(m -> m.getReturnType() == Spliterator.class)//
				.findFirst()//
				.map(m -> stream(() -> tryInvokeSpliterator(object, m), ORDERED, false));//
	}

	private static Optional<Stream<?>> streamFromIteratorSupplier(Object object) {
		return Arrays.stream(object.getClass().getMethods())//
				.filter(m -> m.getReturnType() == Iterator.class)//
				.findFirst()//
				.map(m -> stream(() -> tryIteratorToSpliterator(object, m), ORDERED, false));//
	}

	private static Spliterator<?> tryInvokeSpliterator(Object object, Method method) {
		return Try.call(() -> (Spliterator<?>) method.invoke(object))//
				.getOrThrow(e -> new JUnitException("Cannot invoke method " + method.getName() + " onto " + object, e));//
	}

	private static Spliterator<?> tryIteratorToSpliterator(Object object, Method method) {
		return Try.call(() -> method.invoke(object))//
				.andThen(m -> Try.call(() -> spliteratorUnknownSize((Iterator<?>) m, ORDERED)))//
				.getOrThrow(e -> new JUnitException("Cannot invoke method " + method.getName() + " onto " + object, e));//
	}

}
