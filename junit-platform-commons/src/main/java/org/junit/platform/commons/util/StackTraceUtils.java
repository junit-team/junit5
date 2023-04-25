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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Collection of utilities for working with stack traces.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class StackTraceUtils {

	private static final String JUNIT_PLATFORM_LAUNCHER_PACKAGE = "org.junit.platform.launcher.";
	private static final List<String> ALWAYS_INCLUDED_STACK_TRACE_ELEMENTS = Arrays.asList( //
		"org.junit.jupiter.api.Assertions", //
		"org.junit.jupiter.api.Assumptions" //
	);

	public static void pruneStackTrace(Throwable throwable, String pruningPattern) {
		List<StackTraceElement> prunedStackTrace = new ArrayList<>();

		Predicate<String> includedStackTraceElementPredicate = ClassNamePatternFilterUtils //
				.excludeMatchingClassNames(pruningPattern) //
				.or(ALWAYS_INCLUDED_STACK_TRACE_ELEMENTS::contains);

		List<StackTraceElement> stackTrace = Arrays.asList(throwable.getStackTrace());
		Collections.reverse(stackTrace);

		for (StackTraceElement element : stackTrace) {
			String name = element.getClassName();
			if (name.startsWith(JUNIT_PLATFORM_LAUNCHER_PACKAGE)) {
				// Pruning everything happening before the first JUnit Platform Launcher call
				prunedStackTrace.clear();
			}
			if (includedStackTraceElementPredicate.test(name)) {
				prunedStackTrace.add(element);
			}
		}

		Collections.reverse(prunedStackTrace);
		throwable.setStackTrace(prunedStackTrace.toArray(new StackTraceElement[0]));
	}

}
