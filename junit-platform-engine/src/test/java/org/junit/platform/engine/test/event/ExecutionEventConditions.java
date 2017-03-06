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
import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.FINISHED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.SKIPPED;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.STARTED;
import static org.junit.platform.engine.test.event.ExecutionEvent.byPayload;
import static org.junit.platform.engine.test.event.ExecutionEvent.byTestDescriptor;
import static org.junit.platform.engine.test.event.ExecutionEvent.byType;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.cause;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.status;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.SoftAssertions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.test.event.ExecutionEvent.Type;

/**
 * Collection of AssertJ conditions for {@link ExecutionEvent}.
 *
 * @since 1.0
 */
public class ExecutionEventConditions {

	@SafeVarargs
	public static void assertRecordedExecutionEventsContainsExactly(List<ExecutionEvent> executionEvents,
			Condition<? super ExecutionEvent>... conditions) {
		SoftAssertions softly = new SoftAssertions();
		assertThat(executionEvents).hasSize(conditions.length);
		for (int i = 0; i < conditions.length; i++) {
			softly.assertThat(executionEvents).has(conditions[i], atIndex(i));
		}
		softly.assertAll();
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Condition<ExecutionEvent> event(Condition<? super ExecutionEvent>... conditions) {
		return Assertions.<ExecutionEvent> allOf(conditions);
	}

	public static Condition<ExecutionEvent> engine() {
		return new Condition<>(byTestDescriptor(EngineDescriptor.class::isInstance), "is an engine");
	}

	public static Condition<ExecutionEvent> test(String uniqueIdSubstring) {
		return allOf(test(), uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<ExecutionEvent> test(String uniqueIdSubstring, String displayName) {
		return allOf(test(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
	}

	public static Condition<ExecutionEvent> test() {
		return new Condition<>(byTestDescriptor(TestDescriptor::isTest), "is a test");
	}

	public static Condition<ExecutionEvent> container(Class<?> clazz) {
		return container(clazz.getName());
	}

	public static Condition<ExecutionEvent> container(String uniqueIdSubstring) {
		return allOf(container(), uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<ExecutionEvent> container() {
		return new Condition<>(byTestDescriptor(TestDescriptor::isContainer), "is a container");
	}

	public static Condition<ExecutionEvent> dynamicTestRegistered(String uniqueIdSubstring) {
		return allOf(type(DYNAMIC_TEST_REGISTERED), uniqueIdSubstring(uniqueIdSubstring));
	}

	public static Condition<ExecutionEvent> uniqueIdSubstring(String uniqueIdSubstring) {
		return new Condition<>(
			byTestDescriptor(where(testDescriptor -> testDescriptor.getUniqueId().toString(),
				uniqueId -> uniqueId.contains(uniqueIdSubstring))),
			"descriptor with uniqueId substring \"%s\"", uniqueIdSubstring);
	}

	public static Condition<ExecutionEvent> displayName(String displayName) {
		return new Condition<>(byTestDescriptor(where(TestDescriptor::getDisplayName, isEqual(displayName))),
			"descriptor with display name \"%s\"", displayName);
	}

	public static Condition<ExecutionEvent> skippedWithReason(String expectedReason) {
		return allOf(type(SKIPPED), reason(expectedReason));
	}

	public static Condition<ExecutionEvent> started() {
		return type(STARTED);
	}

	public static Condition<ExecutionEvent> abortedWithReason(Condition<? super Throwable> causeCondition) {
		return finishedWithCause(ABORTED, causeCondition);
	}

	public static Condition<ExecutionEvent> finishedWithFailure(Condition<? super Throwable> causeCondition) {
		return finishedWithCause(FAILED, causeCondition);
	}

	private static Condition<ExecutionEvent> finishedWithCause(Status expectedStatus,
			Condition<? super Throwable> causeCondition) {
		return finished(allOf(status(expectedStatus), cause(causeCondition)));
	}

	public static Condition<ExecutionEvent> finishedWithFailure() {
		return finished(status(FAILED));
	}

	public static Condition<ExecutionEvent> finishedSuccessfully() {
		return finished(status(SUCCESSFUL));
	}

	public static Condition<ExecutionEvent> finished(Condition<TestExecutionResult> resultCondition) {
		return allOf(type(FINISHED), result(resultCondition));
	}

	public static Condition<ExecutionEvent> type(Type expectedType) {
		return new Condition<>(byType(expectedType), "type is %s", expectedType);
	}

	public static Condition<ExecutionEvent> result(Condition<TestExecutionResult> condition) {
		return new Condition<>(byPayload(TestExecutionResult.class, condition::matches), "event with result where %s",
			condition);
	}

	public static Condition<ExecutionEvent> reason(String expectedReason) {
		return new Condition<>(byPayload(String.class, isEqual(expectedReason)), "event with reason '%s'",
			expectedReason);
	}

}
