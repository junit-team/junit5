/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;

import org.apiguardian.api.API;

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
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public final class StringUtils {

	private StringUtils() {
		/* no-op */
	}

	/**
	 * Determine if the supplied {@link String} is <em>blank</em> (i.e.,
	 * {@code null} or consisting only of whitespace characters).
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string is blank
	 * @see #isNotBlank(String)
	 */
	public static boolean isBlank(String str) {
		return (str == null || str.trim().isEmpty());
	}

	/**
	 * Determine if the supplied {@link String} is not {@linkplain #isBlank
	 * blank}.
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string is not blank
	 * @see #isBlank(String)
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * Determine if the supplied {@link String} contains any whitespace characters.
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string contains whitespace
	 * @see #containsIsoControlCharacter(String)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean containsWhitespace(String str) {
		return str != null && str.codePoints().anyMatch(Character::isWhitespace);
	}

	/**
	 * Determine if the supplied {@link String} does not contain any whitespace
	 * characters.
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string does not contain whitespace
	 * @see #containsWhitespace(String)
	 * @see #containsIsoControlCharacter(String)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean doesNotContainWhitespace(String str) {
		return !containsWhitespace(str);
	}

	/**
	 * Determine if the supplied {@link String} contains any ISO control characters.
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string contains an ISO control character
	 * @see #containsWhitespace(String)
	 * @see Character#isISOControl(int)
	 */
	public static boolean containsIsoControlCharacter(String str) {
		return str != null && str.codePoints().anyMatch(Character::isISOControl);
	}

	/**
	 * Determine if the supplied {@link String} does not contain any ISO control
	 * characters.
	 *
	 * @param str the string to check; may be {@code null}
	 * @return {@code true} if the string does not contain an ISO control character
	 * @see #containsIsoControlCharacter(String)
	 * @see #containsWhitespace(String)
	 * @see Character#isISOControl(int)
	 */
	public static boolean doesNotContainIsoControlCharacter(String str) {
		return !containsIsoControlCharacter(str);
	}

	/**
	 * Convert the supplied {@code Object} to a {@code String} using the
	 * following algorithm.
	 *
	 * <ul>
	 * <li>If the supplied object is {@code null}, this method returns {@code "null"}.</li>
	 * <li>If the supplied object is a primitive array, the appropriate
	 * {@code Arrays#toString(...)} variant will be used to convert it to a String.</li>
	 * <li>If the supplied object is an object array, {@code Arrays#deepToString(Object[])}
	 * will be used to convert it to a String.</li>
	 * <li>Otherwise, the result of invoking {@code toString()} on the object
	 * will be returned.</li>
	 * <li>If any of the above results in an exception, the String returned by
	 * this method will be generated using the supplied object's class name and
	 * hash code as follows:
	 * {@code obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj))}</li>
	 * </ul>
	 *
	 * @param obj the object to convert to a String; may be {@code null}
	 * @return a String representation of the supplied object; never {@code null}
	 * @see Arrays#deepToString(Object[])
	 * @see ClassUtils#nullSafeToString(Class...)
	 */
	public static String nullSafeToString(Object obj) {
		if (obj == null) {
			return "null";
		}

		try {
			if (obj.getClass().isArray()) {
				if (obj.getClass().getComponentType().isPrimitive()) {
					if (obj instanceof boolean[]) {
						return Arrays.toString((boolean[]) obj);
					}
					if (obj instanceof char[]) {
						return Arrays.toString((char[]) obj);
					}
					if (obj instanceof short[]) {
						return Arrays.toString((short[]) obj);
					}
					if (obj instanceof byte[]) {
						return Arrays.toString((byte[]) obj);
					}
					if (obj instanceof int[]) {
						return Arrays.toString((int[]) obj);
					}
					if (obj instanceof long[]) {
						return Arrays.toString((long[]) obj);
					}
					if (obj instanceof float[]) {
						return Arrays.toString((float[]) obj);
					}
					if (obj instanceof double[]) {
						return Arrays.toString((double[]) obj);
					}
				}
				return Arrays.deepToString((Object[]) obj);
			}

			// else
			return obj.toString();
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);

			return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
		}
	}

}
