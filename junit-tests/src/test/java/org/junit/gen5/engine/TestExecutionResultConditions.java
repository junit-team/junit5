/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.function.Predicate.isEqual;
import static org.junit.gen5.commons.util.FunctionUtils.where;

import org.assertj.core.api.Condition;
import org.junit.gen5.engine.TestExecutionResult.Status;

/**
 * Collection of AssertJ conditions for {@link TestExecutionResult}.
 */
public class TestExecutionResultConditions {

	public static Condition<TestExecutionResult> status(Status expectedStatus) {
		return new Condition<>(where(TestExecutionResult::getStatus, isEqual(expectedStatus)), "status is %s",
			expectedStatus);
	}

	public static Condition<TestExecutionResult> causeMessage(String expectedMessage) {
		return new Condition<TestExecutionResult>(where(TestExecutionResult::getThrowable, throwable -> {
			return throwable.isPresent() && expectedMessage.equals(throwable.get().getMessage());
		}), "message of cause is %s", expectedMessage);
	}

}
