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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Collection of utilities for working with {@link String Strings},
 * {@link CharSequence CharSequences}, etc.
 *
 * @since 5.0
 */
public final class StringUtils {

	private StringUtils() {
		/* no-op */
	}

	/**
	 * Determine if the supplied {@link CharSequence} is <em>empty</em> (i.e.,
	 * {@code null} or zero-length).
	 *
	 * @param charSequence the {@code CharSequence} to check
	 * @return {@code true} if the {@code CharSequence} is empty
	 * @see #isNotEmpty(CharSequence)
	 * @see #isBlank(String)
	 */
	public static boolean isEmpty(CharSequence charSequence) {
		return (charSequence == null || charSequence.length() == 0);
	}

	/**
	 * Determine if the supplied {@link CharSequence} is not {@linkplain #isEmpty
	 * empty}.
	 *
	 * @param charSequence the {@code CharSequence} to check
	 * @return {@code true} if the {@code CharSequence} is not empty
	 * @see #isEmpty(CharSequence)
	 * @see #isNotBlank(String)
	 */
	public static boolean isNotEmpty(CharSequence charSequence) {
		return !isEmpty(charSequence);
	}

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check
	 * @return {@code true} if the string is blank
	 * @see #isNotBlank(String)
	 * @see #isEmpty(CharSequence)
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().length() == 0);
	}

	/**
	 * Determine if the supplied {@link String} is not {@linkplain #isBlank
	 * blank}.
	 *
	 * @param str the string to check
	 * @return {@code true} if the string is not blank
	 * @see #isBlank(String)
	 * @see #isEmpty(CharSequence)
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * Generate a comma-separated list of class names for the supplied
	 * classes.
	 *
	 * @param classes the classes whose names should be included in the
	 * generated string
	 * @return a comma-separated list of class names, or an empty string if
	 * the supplied class array is {@code null} or empty
	 */
	public static String nullSafeToString(Class<?>... classes) {
		if (classes == null || classes.length == 0) {
			return "";
		}
		return stream(classes).map(Class::getName).collect(joining(", "));
	}

}
