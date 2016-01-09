/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Collection of utilities for asserting preconditions for method and
 * constructor arguments.
 *
 * @since 5.0
 */
public final class Preconditions {

	private Preconditions() {
		/* no-op */
	}

	/**
	 * Assert that the supplied {@link Object} is not {@code null}.
	 *
	 * @param object the object to check
	 * @param message precondition failure message
	 * @return the passed in object
	 * @throws NullPointerException if the object is {@code null}
	 * @see #notNull(Object, Supplier)
	 */
	public static <T> T notNull(T object, String message) throws NullPointerException {
		return notNull(object, () -> message);
	}

	/**
	 * Assert that the supplied {@link Object} is not {@code null}.
	 *
	 * @param object the object to check
	 * @param messageSupplier precondition failure message supplier
	 * @return the passed in object
	 * @throws NullPointerException if the object is {@code null}
	 * @see #condition(boolean, Supplier)
	 */
	public static <T> T notNull(T object, Supplier<String> messageSupplier) throws NullPointerException {
		if (object == null) {
			throw new NullPointerException(messageSupplier.get());
		}
		return object;
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or empty.
	 *
	 * @param str the string to check
	 * @param message precondition failure message
	 * @throws IllegalArgumentException if the string is {@code null} or empty
	 * @see #notEmpty(String, Supplier)
	 */
	public static void notEmpty(String str, String message) throws IllegalArgumentException {
		notEmpty(str, () -> message);
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or empty.
	 *
	 * @param str the string to check
	 * @param messageSupplier precondition failure message supplier
	 * @see StringUtils#isNotEmpty(CharSequence)
	 * @throws IllegalArgumentException if the string is {@code null} or empty
	 * @see #condition(boolean, Supplier)
	 */
	public static void notEmpty(String str, Supplier<String> messageSupplier) throws IllegalArgumentException {
		condition(StringUtils.isNotEmpty(str), messageSupplier);
	}

	/**
	 * Assert that the supplied {@link Collection} is not {@code null} or empty.
	 *
	 * @param collection the collection to check
	 * @param message precondition failure message
	 * @throws IllegalArgumentException if the collection is {@code null} or empty
	 * @see #condition(boolean, Supplier)
	 */
	public static void notEmpty(Collection<?> collection, String message) throws IllegalArgumentException {
		condition(collection != null && !collection.isEmpty(), () -> message);
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or blank.
	 *
	 * @param str the string to check
	 * @param message precondition failure message
	 * @throws IllegalArgumentException if the string is {@code null} or blank
	 * @see #notBlank(String, Supplier)
	 */
	public static void notBlank(String str, String message) throws IllegalArgumentException {
		notBlank(str, () -> message);
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or blank.
	 *
	 * @param str the string to check
	 * @param messageSupplier precondition failure message supplier
	 * @throws IllegalArgumentException if the string is {@code null} or blank
	 * @see StringUtils#isNotBlank(String)
	 * @see #condition(boolean, Supplier)
	 */
	public static void notBlank(String str, Supplier<String> messageSupplier) throws IllegalArgumentException {
		condition(StringUtils.isNotBlank(str), messageSupplier);
	}

	/**
	 * Assert that the supplied {@code predicate} is {@code true}.
	 *
	 * @param predicate the predicate to check
	 * @param message precondition failure message
	 * @throws IllegalArgumentException if the predicate is {@code false}
	 * @see #condition(boolean, Supplier)
	 */
	public static void condition(boolean predicate, String message) throws IllegalArgumentException {
		condition(predicate, () -> message);
	}

	/**
	 * Assert that the supplied {@code predicate} is {@code true}.
	 *
	 * @param predicate the predicate to check
	 * @param messageSupplier precondition failure message supplier
	 * @throws IllegalArgumentException if the predicate is {@code false}
	 */
	public static void condition(boolean predicate, Supplier<String> messageSupplier) throws IllegalArgumentException {
		if (!predicate) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
	}

}
