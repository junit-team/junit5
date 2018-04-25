/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.tck;

import static org.junit.platform.commons.util.FunctionUtils.where;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

@API(status = API.Status.EXPERIMENTAL, since = "1.2.0")
public class ExecutionEvent {

	private static final Supplier<IllegalArgumentException> EXCEPTION_NO_PAYLOAD = () -> new IllegalArgumentException(
		"Cannot access payload from ExecutionEvent when no payload is present.");

	private Type type;
	private TestDescriptor testDescriptor;
	private Object payload;
	private LocalDateTime dateTimeOccurred;

	private ExecutionEvent(ExecutionEvent.Type type, TestDescriptor testDescriptor, Object payload) {
		this.type = type;
		this.testDescriptor = testDescriptor;
		this.payload = payload;
		this.dateTimeOccurred = LocalDateTime.now();
	}

	public static ExecutionEvent reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		return new ExecutionEvent(Type.REPORTING_ENTRY_PUBLISHED, testDescriptor, entry);
	}

	public static ExecutionEvent dynamicTestRegistered(TestDescriptor testDescriptor) {
		return new ExecutionEvent(Type.DYNAMIC_TEST_REGISTERED, testDescriptor, null);
	}

	public static ExecutionEvent executionSkipped(TestDescriptor testDescriptor, String reason) {
		return new ExecutionEvent(Type.SKIPPED, testDescriptor, reason);
	}

	public static ExecutionEvent executionStarted(TestDescriptor testDescriptor) {
		return new ExecutionEvent(Type.STARTED, testDescriptor, null);
	}

	public static ExecutionEvent executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		return new ExecutionEvent(Type.FINISHED, testDescriptor, result);
	}

	public static <T> Predicate<ExecutionEvent> byPayload(Class<T> payloadClass, Predicate<? super T> predicate) {
		return event -> event.getPayload(payloadClass).filter(predicate).isPresent();
	}

	public static Predicate<ExecutionEvent> byType(ExecutionEvent.Type type) {
		return event -> event.type.equals(type);
	}

	public static Predicate<ExecutionEvent> byTestDescriptor(Predicate<? super TestDescriptor> predicate) {
		return where(ExecutionEvent::getTestDescriptor, predicate);
	}

	public ExecutionEvent.Type getType() {
		return type;
	}

	public TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	public LocalDateTime getDateTimeOccurred() {
		return dateTimeOccurred;
	}

	public <T> Optional<T> getPayload(Class<T> payloadClass) {
		return Optional.ofNullable(payload).filter(payloadClass::isInstance).map(payloadClass::cast);
	}

	public boolean isPayloadPresent(Class<?> payloadClass) {
		return getPayload(payloadClass).isPresent();
	}

	public <T> T getPayloadAs(Class<T> payloadClass) {
		return getPayload(payloadClass).orElseThrow(EXCEPTION_NO_PAYLOAD);
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

	public enum Type {
		DYNAMIC_TEST_REGISTERED, SKIPPED, STARTED, FINISHED, REPORTING_ENTRY_PUBLISHED
	}

}
