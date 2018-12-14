/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.testkit.engine.Event.byPayload;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;
import static org.junit.platform.testkit.engine.Event.byType;
import static org.junit.platform.testkit.engine.EventType.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.testkit.engine.EventType.FINISHED;
import static org.junit.platform.testkit.engine.EventType.SKIPPED;
import static org.junit.platform.testkit.engine.EventType.STARTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/**
 * Collection of AssertJ {@linkplain Condition conditions} for {@link Event}.
 *
 * @since 1.4
 * @see TestExecutionResultConditions
 */
@API(status = EXPERIMENTAL, since = "1.4")
public final class EventConditions {

	private EventConditions() {
		/* no-op */
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Condition<Event> event(Condition<? super Event>... conditions) {
		return Assertions.allOf(conditions);
	}

	public static Condition<Event> engine() {
		return new Condition<>(byTestDescriptor(EngineDescriptor.class::isInstance), "is an engine");
	}

	public static Condition<Event> test(String uniqueIdSubstring) {
		return Assertions.allOf(test(), uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<Event> test(String uniqueIdSubstring, String displayName) {
		return Assertions.allOf(test(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
	}

	public static Condition<Event> test() {
		return new Condition<>(byTestDescriptor(TestDescriptor::isTest), "is a test");
	}

	public static Condition<Event> container(Class<?> clazz) {
		return container(clazz.getName());
	}

	public static Condition<Event> container(String uniqueIdSubstring) {
		return container(uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<Event> container(Condition<Event> condition) {
		return Assertions.allOf(container(), condition);
	}

	public static Condition<Event> container() {
		return new Condition<>(byTestDescriptor(TestDescriptor::isContainer), "is a container");
	}

	public static Condition<Event> nestedContainer(Class<?> clazz) {
		return Assertions.allOf(container(uniqueIdSubstring(clazz.getEnclosingClass().getName())),
			container(uniqueIdSubstring(clazz.getSimpleName())));
	}

	public static Condition<Event> dynamicTestRegistered(String uniqueIdSubstring) {
		return dynamicTestRegistered(uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<Event> dynamicTestRegistered(Condition<Event> condition) {
		return Assertions.allOf(type(DYNAMIC_TEST_REGISTERED), condition);
	}

	public static Condition<Event> uniqueIdSubstring(String uniqueIdSubstring) {
		Predicate<UniqueId.Segment> predicate = segment -> {
			String text = segment.getType() + ":" + segment.getValue();
			return text.contains(uniqueIdSubstring);
		};

		return new Condition<>(
			byTestDescriptor(
				where(TestDescriptor::getUniqueId, uniqueId -> uniqueId.getSegments().stream().anyMatch(predicate))),
			"descriptor with uniqueId substring '%s'", uniqueIdSubstring);
	}

	public static Condition<Event> displayName(String displayName) {
		return new Condition<>(byTestDescriptor(where(TestDescriptor::getDisplayName, isEqual(displayName))),
			"descriptor with display name '%s'", displayName);
	}

	public static Condition<Event> skippedWithReason(String expectedReason) {
		return Assertions.allOf(type(SKIPPED), reason(expectedReason));
	}

	public static Condition<Event> skippedWithReason(Predicate<String> predicate) {
		return Assertions.allOf(type(SKIPPED), reason(predicate));
	}

	public static Condition<Event> started() {
		return type(STARTED);
	}

	public static Condition<Event> abortedWithReason(Condition<? super Throwable> causeCondition) {
		return finishedWithCause(ABORTED, causeCondition);
	}

	@SafeVarargs
	public static Condition<Event> abortedWithReason(Condition<Throwable>... conditions) {
		return finishedWithCause(ABORTED, conditions);
	}

	public static Condition<Event> finishedWithFailure(Condition<? super Throwable> causeCondition) {
		return finishedWithCause(FAILED, causeCondition);
	}

	@SafeVarargs
	public static Condition<Event> finishedWithFailure(Condition<Throwable>... conditions) {
		return finishedWithCause(FAILED, conditions);
	}

	@SuppressWarnings("unchecked")
	private static Condition<Event> finishedWithCause(Status expectedStatus, Condition<Throwable>... conditions) {

		List<Condition<TestExecutionResult>> list = Arrays.stream(conditions)//
				.map(TestExecutionResultConditions::throwable)//
				.collect(toCollection(ArrayList::new));

		list.add(0, TestExecutionResultConditions.status(expectedStatus));

		return finished(Assertions.allOf(list));
	}

	private static Condition<Event> finishedWithCause(Status expectedStatus,
			Condition<? super Throwable> causeCondition) {

		return finished(Assertions.allOf(TestExecutionResultConditions.status(expectedStatus),
			TestExecutionResultConditions.throwable(causeCondition)));
	}

	public static Condition<Event> finishedWithFailure() {
		return finished(TestExecutionResultConditions.status(FAILED));
	}

	public static Condition<Event> finishedSuccessfully() {
		return finished(TestExecutionResultConditions.status(SUCCESSFUL));
	}

	public static Condition<Event> finished(Condition<TestExecutionResult> resultCondition) {
		return Assertions.allOf(type(FINISHED), result(resultCondition));
	}

	public static Condition<Event> type(EventType expectedType) {
		return new Condition<>(byType(expectedType), "type is %s", expectedType);
	}

	public static Condition<Event> result(Condition<TestExecutionResult> condition) {
		return new Condition<>(byPayload(TestExecutionResult.class, condition::matches), "event with result where %s",
			condition);
	}

	public static Condition<Event> reason(String expectedReason) {
		return new Condition<>(byPayload(String.class, isEqual(expectedReason)), "event with reason '%s'",
			expectedReason);
	}

	public static Condition<Event> reason(Predicate<String> predicate) {
		return new Condition<>(byPayload(String.class, predicate), "event with custom reason predicate");
	}

}
