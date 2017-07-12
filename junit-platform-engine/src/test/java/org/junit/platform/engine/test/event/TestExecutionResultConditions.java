/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.test.event;

import static java.util.function.Predicate.isEqual;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.util.function.Predicate;

import org.assertj.core.api.Condition;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;

/**
 * Collection of AssertJ conditions for {@link TestExecutionResult}.
 *
 * @since 1.0
 */
public class TestExecutionResultConditions {

	public static Condition<TestExecutionResult> status(Status expectedStatus) {
		return new Condition<>(where(TestExecutionResult::getStatus, isEqual(expectedStatus)), "status is %s",
			expectedStatus);
	}

	public static Condition<Throwable> message(String expectedMessage) {
		return new Condition<>(where(Throwable::getMessage, isEqual(expectedMessage)), "message is \"%s\"",
			expectedMessage);
	}

	public static Condition<Throwable> message(Predicate<String> predicate) {
		return new Condition<>(where(Throwable::getMessage, predicate), "message is \"%s\"", predicate);
	}

	public static Condition<Throwable> isA(Class<? extends Throwable> expectedClass) {
		return new Condition<>(expectedClass::isInstance, "instance of %s", expectedClass.getName());
	}

	public static Condition<Throwable> suppressed(int index, Condition<Throwable> checked) {
		return new Condition<>(throwable -> checked.matches(throwable.getSuppressed()[index]),
			"suppressed at index %d matches %s", index, checked);

	}

	public static Condition<TestExecutionResult> cause(Condition<? super Throwable> condition) {
		return new Condition<TestExecutionResult>(where(TestExecutionResult::getThrowable, throwable -> {
			return throwable.isPresent() && condition.matches(throwable.get());
		}), "cause where %s", condition);
	}

}
