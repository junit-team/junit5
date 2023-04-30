/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.platform.commons.util.ClassNamePatternFilterUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Prunes the stack trace in case of a failed event.
 *
 * @since 1.10
 * @see org.junit.platform.commons.util.ExceptionUtils#pruneStackTrace(Throwable, Predicate)
 */
class StackTracePruningEngineExecutionListener extends DelegatingEngineExecutionListener {

	public static final String STACKTRACE_PRUNING_ENABLED_PROPERTY_NAME = "junit.platform.stacktrace.pruning.enabled";
	public static final String STACKTRACE_PRUNING_PATTERN_PROPERTY_NAME = "junit.platform.stacktrace.pruning.pattern";
	public static final String STACKTRACE_PRUNING_DEFAULT_PATTERN = "org.junit.*,java.*";
	private static final List<String> ALWAYS_INCLUDED_STACK_TRACE_ELEMENTS = Arrays.asList( //
		"org.junit.jupiter.api.Assertions", //
		"org.junit.jupiter.api.Assumptions" //
	);

	private final Predicate<String> stackTraceElementFilter;

	StackTracePruningEngineExecutionListener(EngineExecutionListener delegate, String pruningPattern) {
		super(delegate);
		this.stackTraceElementFilter = ClassNamePatternFilterUtils.excludeMatchingClassNames(pruningPattern) //
				.or(ALWAYS_INCLUDED_STACK_TRACE_ELEMENTS::contains);
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		if (testExecutionResult.getThrowable().isPresent()) {
			Throwable throwable = testExecutionResult.getThrowable().get();

			ExceptionUtils.findNestedThrowables(throwable).forEach(this::pruneStackTrace);
		}
		super.executionFinished(testDescriptor, testExecutionResult);
	}

	private void pruneStackTrace(Throwable throwable) {
		ExceptionUtils.pruneStackTrace(throwable, stackTraceElementFilter);
	}

}
