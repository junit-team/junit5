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

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.gen5.commons.meta.API;

/**
 * Collection of utilities for asserting preconditions for method and
 * constructor arguments.
 *
 * <p>Each method in this class throws a {@link PreconditionViolationException}
 * if the precondition is violated.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.0
 */
@API(Internal)
public abstract class Preconditions {

	/**
	 * Assert that the supplied {@link Object} is not {@code null}.
	 *
	 * @param object the object to check
	 * @param message precondition violation message
	 * @return the supplied object as a convenience
	 * @throws PreconditionViolationException if the supplied object is {@code null}
	 * @see #notNull(Object, Supplier)
	 */
	public static <T> T notNull(T object, String message) throws PreconditionViolationException {
		return notNull(object, () -> message);
	}

	/**
	 * Assert that none of the supplied {@link Object}s are {@code null}.
	 *
	 * @param objects the objects to check
	 * @param message precondition violation message
	 * @return the supplied objects as a convenience
	 * @throws PreconditionViolationException if any of the supplied objects are {@code null}
	 * @see #notNull(Object, Supplier)
	 */
	public static Object[] notNull(Object[] objects, String message) throws PreconditionViolationException {
		notNull(objects, () -> "objects array must not be null");
		Arrays.stream(objects).forEach(object -> notNull(object, () -> message));

		return objects;
	}

	/**
	 * Assert that the supplied {@link Object} is not {@code null}.
	 *
	 * @param object the object to check
	 * @param messageSupplier precondition violation message supplier
	 * @return the supplied object as a convenience
	 * @throws PreconditionViolationException if the supplied object is {@code null}
	 * @see #condition(boolean, Supplier)
	 */
	public static <T> T notNull(T object, Supplier<String> messageSupplier) throws PreconditionViolationException {
		condition(object != null, messageSupplier);
		return object;
	}

	/**
	 * Assert that the supplied {@link Collection} is not {@code null} or empty.
	 *
	 * @param collection the collection to check
	 * @param message precondition violation message
	 * @return the supplied collection as a convenience
	 * @throws PreconditionViolationException if the supplied collection is {@code null} or empty
	 * @see #condition(boolean, Supplier)
	 */
	public static <T extends Collection<?>> T notEmpty(T collection, String message)
			throws PreconditionViolationException {
		condition(collection != null && !collection.isEmpty(), () -> message);
		return collection;
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or blank.
	 *
	 * @param str the string to check
	 * @param message precondition violation message
	 * @return the supplied string as a convenience
	 * @throws PreconditionViolationException if the supplied string is {@code null} or blank
	 * @see #notBlank(String, Supplier)
	 */
	public static String notBlank(String str, String message) throws PreconditionViolationException {
		return notBlank(str, () -> message);
	}

	/**
	 * Assert that the supplied {@link String} is not {@code null} or blank.
	 *
	 * @param str the string to check
	 * @param messageSupplier precondition violation message supplier
	 * @return the supplied string as a convenience
	 * @throws PreconditionViolationException if the supplied string is {@code null} or blank
	 * @see StringUtils#isNotBlank(String)
	 * @see #condition(boolean, Supplier)
	 */
	public static String notBlank(String str, Supplier<String> messageSupplier) throws PreconditionViolationException {
		condition(StringUtils.isNotBlank(str), messageSupplier);
		return str;
	}

	/**
	 * Assert that the supplied {@code predicate} is {@code true}.
	 *
	 * @param predicate the predicate to check
	 * @param message precondition violation message
	 * @throws PreconditionViolationException if the predicate is {@code false}
	 * @see #condition(boolean, Supplier)
	 */
	public static void condition(boolean predicate, String message) throws PreconditionViolationException {
		condition(predicate, () -> message);
	}

	/**
	 * Assert that the supplied {@code predicate} is {@code true}.
	 *
	 * @param predicate the predicate to check
	 * @param messageSupplier precondition violation message supplier
	 * @throws PreconditionViolationException if the predicate is {@code false}
	 */
	public static void condition(boolean predicate, Supplier<String> messageSupplier)
			throws PreconditionViolationException {
		if (!predicate) {
			throw new PreconditionViolationException(messageSupplier.get());
		}
	}

}
