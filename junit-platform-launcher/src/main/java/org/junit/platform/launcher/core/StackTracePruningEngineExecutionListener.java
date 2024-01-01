/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Prunes the stack trace in case of a failed event.
 *
 * @since 1.10
 * @see org.junit.platform.commons.util.ExceptionUtils#pruneStackTrace(Throwable, List)
 */
class StackTracePruningEngineExecutionListener extends DelegatingEngineExecutionListener {

	StackTracePruningEngineExecutionListener(EngineExecutionListener delegate) {
		super(delegate);
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		List<String> testClassNames = getTestClassNames(testDescriptor);
		if (testExecutionResult.getThrowable().isPresent()) {
			Throwable throwable = testExecutionResult.getThrowable().get();

			ExceptionUtils.findNestedThrowables(throwable).forEach(
				t -> ExceptionUtils.pruneStackTrace(t, testClassNames));
		}
		super.executionFinished(testDescriptor, testExecutionResult);
	}

	private static List<String> getTestClassNames(TestDescriptor testDescriptor) {
		return testDescriptor.getAncestors() //
				.stream() //
				.map(TestDescriptor::getSource) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.map(source -> {
					if (source instanceof ClassSource) {
						return ((ClassSource) source).getClassName();
					}
					else if (source instanceof MethodSource) {
						return ((MethodSource) source).getClassName();
					}
					else {
						return null;
					}
				}) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
	}

}
