/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Abstract base class for unit testing a concrete {@link ExecutionCondition}
 * implementation.
 *
 * @since 5.1
 */
abstract class AbstractExecutionConditionTests {

	private final ExtensionContext context = mock(ExtensionContext.class);

	private ConditionEvaluationResult result;

	@BeforeEach
	void beforeEach(TestInfo testInfo) {
		when(this.context.getElement()).thenReturn(method(testInfo));
	}

	protected abstract ExecutionCondition getExecutionCondition();

	protected abstract Class<?> getTestClass();

	protected void evaluateCondition() {
		this.result = getExecutionCondition().evaluateExecutionCondition(this.context);
	}

	protected void assertEnabled() {
		assertTrue(!this.result.isDisabled(), "Should be enabled");
	}

	protected void assertDisabled() {
		assertTrue(this.result.isDisabled(), "Should be disabled");
	}

	protected void assertReasonContains(String text) {
		assertThat(this.result.getReason()).hasValueSatisfying(reason -> assertThat(reason).contains(text));
	}

	private Optional<AnnotatedElement> method(TestInfo testInfo) {
		return method(getTestClass(), testInfo.getTestMethod().get().getName());
	}

	private Optional<AnnotatedElement> method(Class<?> testClass, String methodName) {
		return Optional.of(ReflectionUtils.findMethod(testClass, methodName).get());
	}

}
