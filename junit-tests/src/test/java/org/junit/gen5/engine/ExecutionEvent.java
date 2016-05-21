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
import static org.junit.gen5.engine.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.gen5.engine.ExecutionEvent.Type.FINISHED;
import static org.junit.gen5.engine.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.gen5.engine.ExecutionEvent.Type.SKIPPED;
import static org.junit.gen5.engine.ExecutionEvent.Type.STARTED;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.engine.reporting.ReportEntry;

/**
 * Represents an event collected by {@link ExecutionEventRecorder}.
 *
 * @see ExecutionEventConditions
 */
public class ExecutionEvent {

	public enum Type {
		DYNAMIC_TEST_REGISTERED, SKIPPED, STARTED, FINISHED, REPORTING_ENTRY_PUBLISHED
	}

	public static ExecutionEvent reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		return new ExecutionEvent(REPORTING_ENTRY_PUBLISHED, testDescriptor, entry);
	}

	public static ExecutionEvent dynamicTestRegistered(TestDescriptor testDescriptor) {
		return new ExecutionEvent(DYNAMIC_TEST_REGISTERED, testDescriptor, null);
	}

	public static ExecutionEvent executionSkipped(TestDescriptor testDescriptor, String reason) {
		return new ExecutionEvent(SKIPPED, testDescriptor, reason);
	}

	public static ExecutionEvent executionStarted(TestDescriptor testDescriptor) {
		return new ExecutionEvent(STARTED, testDescriptor, null);
	}

	public static ExecutionEvent executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		return new ExecutionEvent(FINISHED, testDescriptor, result);
	}

	public static Predicate<ExecutionEvent> byType(ExecutionEvent.Type type) {
		return where(ExecutionEvent::getType, isEqual(type));
	}

	public static Predicate<ExecutionEvent> byTestDescriptor(Predicate<? super TestDescriptor> predicate) {
		return where(ExecutionEvent::getTestDescriptor, predicate);
	}

	public static <T> Predicate<ExecutionEvent> byPayload(Class<T> payloadClass, Predicate<? super T> predicate) {
		return event -> event.getPayload(payloadClass).filter(predicate).isPresent();
	}

	private final ExecutionEvent.Type type;
	private final TestDescriptor testDescriptor;
	private final Object payload;

	private ExecutionEvent(ExecutionEvent.Type type, TestDescriptor testDescriptor, Object payload) {
		this.type = type;
		this.testDescriptor = testDescriptor;
		this.payload = payload;
	}

	public ExecutionEvent.Type getType() {
		return type;
	}

	public TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	public <T> Optional<T> getPayload(Class<T> payloadClass) {
		return Optional.ofNullable(payload).filter(payloadClass::isInstance).map(payloadClass::cast);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("type", type)
				.append("testDescriptor", testDescriptor)
				.append("payload", payload)
				.toString();
		// @formatter:on
	}

}
