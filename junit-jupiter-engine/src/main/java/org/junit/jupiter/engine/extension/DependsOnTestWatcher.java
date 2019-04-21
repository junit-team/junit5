/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DependsOn;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * {@link ExecutionCondition} that supports the {@code @DependsOn} annotation.
 *
 * @since 5.5
 * @see DependsOn
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
public class DependsOnTestWatcher implements ExecutionCondition, TestWatcher {
	/**
	 * successfulTests stores tests that are successfully executed
	 */
	private Set<String> successfulTests = new HashSet<>();

	@Override
	public void testDisabled(ExtensionContext context, Optional<String> reason) {
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		context.getTestMethod().ifPresent(method -> successfulTests.add(method.getName()));
	}

	@Override
	public void testAborted(ExtensionContext context, Throwable cause) {
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Method method = context.getTestMethod().orElse(null);

		if (method != null) {
			DependsOn annotation = method.getAnnotation(DependsOn.class);
			if (annotation != null) {
				// loop through all dependent methods' name
				Optional<String> unsuccessfulMethod = Arrays.stream(annotation.value()).filter(
					name -> !successfulTests.contains(name)).findAny();

				if (unsuccessfulMethod.isPresent()) {
					// disable this test
					return ConditionEvaluationResult.disabled(String.format(
						"'%s()' cannot be executed because its dependent test '%s()' either failed or just did not execute!",
						method.getName(), unsuccessfulMethod.get()));
				}
			}
		}

		return ConditionEvaluationResult.enabled("Enable by Default");
	}
}
