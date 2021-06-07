/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.lang.StackWalker.StackFrame;
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

	static {
		// No source information? => Not useful
		ignoreClass(className -> className == null || className.isEmpty());

		ignoreClassStartingWith("java");
		ignoreClass(TestSourceLocator.class.getName());
	}

	/**
	 * Ignore matching class names.
	 *
	 * @param filter The filter condition to add.
	 */
	public static void ignoreClass(final Predicate<String> filter) {
		Preconditions.notNull(filter, "filter must not be null");
		StackWalkerBased.sourceFilters.add(ste -> filter.test(ste.getClassName()));
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
		return StackWalker.getInstance() //
				.walk(stream -> stream.filter(StackWalkerBased::isntIgnored).findFirst()) //
				.map(StackWalkerBased::toUri).orElse(null);
	}

	/**
	 * Helper for StackFrame filtering. Required due to the use of multi release jars.
	 */
	@API(status = Status.INTERNAL, since = "1.8")
	private static final class StackWalkerBased {

		private StackWalkerBased() {
			/* no-op */
		}

		/**
		 * A set containing a list of source filters. Matching elements will be ignored.
		 */
		private static final Set<Predicate<StackFrame>> sourceFilters = new LinkedHashSet<>();

		/**
		 * Checks whether the given element does not match any of the ignore filters.
		 *
		 * @param element The element to check.
		 * @return True, if the given element does not match any filter. False otherwise.
		 */
		private static boolean isntIgnored(final StackFrame element) {
			for (final Predicate<StackFrame> filter : sourceFilters) {
				if (filter.test(element)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Converts the given element to a TestSource uri.
		 *
		 * @param element The element to convert.
		 * @return The uri representing the test source described by the given element.
		 */
		private static URI toUri(final StackFrame element) {
			final String className = element.getClassName();
			final String rootClassName = className.replaceFirst("\\$.*", "");
			final int lineNumber = element.getLineNumber();
			return URI.create("class:" + rootClassName + (lineNumber > 0 ? "?line=" + lineNumber : ""));
		}

	}

}
