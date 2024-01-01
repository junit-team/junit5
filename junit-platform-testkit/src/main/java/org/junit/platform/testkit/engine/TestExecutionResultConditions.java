/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;

/**
 * Collection of AssertJ {@linkplain Condition conditions} for
 * {@link TestExecutionResult}.
 *
 * @since 1.4
 * @see EventConditions
 */
@API(status = MAINTAINED, since = "1.7")
public final class TestExecutionResultConditions {

	private TestExecutionResultConditions() {
		/* no-op */
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link TestExecutionResult}'s {@linkplain TestExecutionResult#getStatus()
	 * status} is equal to the supplied {@link Status Status}.
	 */
	public static Condition<TestExecutionResult> status(Status expectedStatus) {
		return new Condition<>(where(TestExecutionResult::getStatus, isEqual(expectedStatus)), "status is %s",
			expectedStatus);
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link TestExecutionResult}'s
	 * {@linkplain TestExecutionResult#getThrowable() throwable} matches all
	 * supplied conditions.
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Condition<TestExecutionResult> throwable(Condition<Throwable>... conditions) {
		List<Condition<TestExecutionResult>> list = Arrays.stream(conditions)//
				.map(TestExecutionResultConditions::throwable)//
				.collect(toList());

		return Assertions.allOf(list);
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link Throwable}'s {@linkplain Throwable#getCause() cause} matches all
	 * supplied conditions.
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Condition<Throwable> cause(Condition<Throwable>... conditions) {
		List<Condition<Throwable>> list = Arrays.stream(conditions)//
				.map(TestExecutionResultConditions::cause)//
				.collect(toList());

		return Assertions.allOf(list);
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link Throwable}'s {@linkplain Throwable#getSuppressed() suppressed
	 * throwable} at the supplied index matches all supplied conditions.
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Condition<Throwable> suppressed(int index, Condition<Throwable>... conditions) {
		List<Condition<Throwable>> list = Arrays.stream(conditions)//
				.map(condition -> suppressed(index, condition))//
				.collect(toList());

		return Assertions.allOf(list);
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link Throwable} is an {@linkplain Class#isInstance(Object) instance of}
	 * the supplied {@link Class}.
	 */
	public static Condition<Throwable> instanceOf(Class<? extends Throwable> expectedType) {
		return new Condition<>(expectedType::isInstance, "instance of %s", expectedType.getName());
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link Throwable}'s {@linkplain Throwable#getMessage() message} is equal
	 * to the supplied {@link String}.
	 */
	public static Condition<Throwable> message(String expectedMessage) {
		return new Condition<>(where(Throwable::getMessage, isEqual(expectedMessage)), "message is '%s'",
			expectedMessage);
	}

	/**
	 * Create a new {@link Condition} that matches if and only if a
	 * {@link Throwable}'s {@linkplain Throwable#getMessage() message} matches
	 * the supplied {@link Predicate}.
	 */
	public static Condition<Throwable> message(Predicate<String> expectedMessagePredicate) {
		return new Condition<>(where(Throwable::getMessage, expectedMessagePredicate), "message matches predicate");
	}

	private static Condition<TestExecutionResult> throwable(Condition<? super Throwable> condition) {
		return new Condition<>(
			where(TestExecutionResult::getThrowable,
				throwable -> throwable.isPresent() && condition.matches(throwable.get())),
			"throwable matches %s", condition);
	}

	private static Condition<Throwable> cause(Condition<Throwable> condition) {
		return new Condition<>(throwable -> condition.matches(throwable.getCause()), "throwable cause matches %s",
			condition);
	}

	private static Condition<Throwable> suppressed(int index, Condition<Throwable> condition) {
		return new Condition<>(
			throwable -> throwable.getSuppressed().length > index
					&& condition.matches(throwable.getSuppressed()[index]),
			"suppressed throwable at index %d matches %s", index, condition);
	}

}
