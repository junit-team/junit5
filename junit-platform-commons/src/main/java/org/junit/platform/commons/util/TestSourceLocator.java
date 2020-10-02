/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Utility class used to locate the source location of dynamically created tests.
 */
@API(status = Status.EXPERIMENTAL, since = "1.8")
public final class TestSourceLocator {

	private TestSourceLocator() {
		/* no-op */
	}

	/**
	 * A set containing a list of source filters. Matching elements will be ignored.
	 */
	private static final Set<Predicate<StackTraceElement>> sourceFilters = new LinkedHashSet<>();

	static {
		// No source information? => Not useful
		ignoreClass(className -> className == null || className.isEmpty());

		ignoreClassStartingWith("java");
		ignoreClass(TestSourceLocator.class.getName());
	}

	/**
	 * Ignore matching elements.
	 *
	 * @param filter The filter condition to add.
	 */
	public static void ignore(final Predicate<StackTraceElement> filter) {
		Preconditions.notNull(filter, "filter must not be null");
		sourceFilters.add(filter);
	}

	/**
	 * Ignore matching class names.
	 *
	 * @param filter The filter condition to add.
	 */
	public static void ignoreClass(final Predicate<String> filter) {
		Preconditions.notNull(filter, "filter must not be null");
		ignore(ste -> filter.test(ste.getClassName()));
	}

	/**
	 * Ignore the given class.
	 *
	 * @param ignored The class name to ignore.
	 */
	public static void ignoreClass(final String ignored) {
		Preconditions.notBlank(ignored, "ignored must not be blank");
		ignoreClass(className -> className.equals(ignored));
	}

	/**
	 * Ignore the given class.
	 *
	 * @param ignored The class to ignore.
	 */
	public static void ignoreClass(final Class<?> ignored) {
		Preconditions.notNull(ignored, "ignored must not be null");
		ignoreClass(ignored.getName());
	}

	/**
	 * Ignore class names with the given prefix, useful to ignore entire packages and nested classes.
	 *
	 * @param prefix The class name prefix to ignore.
	 */
	public static void ignoreClassStartingWith(final String prefix) {
		Preconditions.notBlank(prefix, "prefix must not be blank");
		ignoreClass(className -> className.startsWith(prefix));
	}

	/**
	 * Locate the testSource uri from the current stacktrace. Elements that match the previously added
	 * filters will be ignored.
	 *
	 * @return The test source uri for the current thread. Null, if it couldn't be determined.
	 */
	public static URI locateSource() {
		for (final StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			if (!matchesFilter(ste)) {
				return toUri(ste);
			}
		}
		return null;
	}

	/**
	 * Check whether the given element matches any of the ignore filters.
	 *
	 * @param ste The element to check.
	 * @return True, if the given element matches any filter. False otherwise.
	 */
	private static boolean matchesFilter(final StackTraceElement ste) {
		for (final Predicate<StackTraceElement> filter : sourceFilters) {
			if (filter.test(ste)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts the given element to a TestSource uri.
	 *
	 * @param ste The element to convert.
	 * @return The uri representing the test source described bythe given element.
	 */
	private static URI toUri(final StackTraceElement ste) {
		final String className = ste.getClassName();
		final String rootClassName = className.replaceFirst("\\$.*", "");
		final int lineNumber = ste.getLineNumber();
		return URI.create("class:/" + rootClassName + (lineNumber > 0 ? "?line=" + lineNumber : ""));
	}

}
