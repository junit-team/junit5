/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DependsOn;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class DependsOnTestWatcher implements ExecutionCondition, TestWatcher {
	private Set<String> unfinishedTests = new HashSet<>();

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
		context.getTestMethod().ifPresent(method -> unfinishedTests.add(method.getName()));
	}

	@Override
	public void testSuccessful(ExtensionContext context) {

	}

	@Override
	public void testAborted(ExtensionContext context, Throwable cause) {
		context.getTestMethod().ifPresent(method -> unfinishedTests.add(method.getName()));
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		context.getTestMethod().ifPresent(method -> unfinishedTests.add(method.getName()));
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Method method = context.getTestMethod().orElse(null);

		if (method != null) {
			DependsOn annotation = method.getAnnotation(DependsOn.class);
			if (annotation != null) {
				for (String name : annotation.value()) {
					if (unfinishedTests.contains(name)) {
						return ConditionEvaluationResult.disabled(String.format(
							"'%s()' cannot be executed because its dependent test '%s()' either failed or just did not execute!",
							method.getName(), name));
					}
				}
			}
		}

		return ConditionEvaluationResult.enabled("Enable by Default");
	}
}
