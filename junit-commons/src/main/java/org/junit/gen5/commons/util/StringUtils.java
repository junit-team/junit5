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
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.stream.Stream;

import org.junit.gen5.commons.meta.API;

/**
 * Collection of utilities for working with {@link String Strings},
 * {@link CharSequence CharSequences}, etc.
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
public final class StringUtils {

	private StringUtils() {
		/* no-op */
	}

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check
	 * @return {@code true} if the string is blank
	 * @see #isNotBlank(String)
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
		return join(stream(classes).map(Class::getName), ", ");
	}

	/**
	 * Join a stream of {@link String}s together.
	 *
	 * @param stringStream the stream of Stings
	 * @param delimiter the delimiter between elements
	 * @return the joined string
	 */
	public static String join(Stream<String> stringStream, String delimiter) {
		return stringStream.collect(joining(delimiter));
	}

}
