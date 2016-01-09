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
 * <p>Each method in this class throws an {@link IllegalArgumentException}
 * if the precondition fails.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
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
	 * @see #notNull(Object, Supplier)
	 */
	public static void notNull(Object object, String message) throws IllegalArgumentException {
		notNull(object, () -> message);
	}

	/**
	 * Assert that the supplied {@link Object} is not {@code null}.
	 *
	 * @param object the object to check
	 * @param messageSupplier precondition failure message supplier
	 * @see #condition(boolean, Supplier)
	 */
	public static void notNull(Object object, Supplier<String> messageSupplier) throws IllegalArgumentException {
		condition(object != null, messageSupplier);
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or empty.
	 *
	 * @param str the string to check
	 * @param message precondition failure message
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
