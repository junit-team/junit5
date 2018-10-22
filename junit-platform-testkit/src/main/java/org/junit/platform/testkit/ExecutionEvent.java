/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * {@code ExecutionEvent} represents a single event fired during execution of
 * a test plan on the JUnit Platform.
 *
 * @since 1.4
 * @see EventType
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class ExecutionEvent {

	// --- Factories -----------------------------------------------------------

	/**
	 * Create an {@code ExecutionEvent} for a reporting entry published for the
	 * supplied {@link TestDescriptor} and {@link ReportEntry}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param entry the {@code ReportEntry} that was published; never {@code null}
	 * @return the newly created {@code ExecutionEvent}
	 * @see EventType#REPORTING_ENTRY_PUBLISHED
	 */
	public static ExecutionEvent reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		Preconditions.notNull(entry, "ReportEntry must not be null");
		return new ExecutionEvent(EventType.REPORTING_ENTRY_PUBLISHED, testDescriptor, entry);
	}

	/**
	 * Create an {@code ExecutionEvent} for the dynamic registration of the
	 * supplied {@link TestDescriptor}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @return the newly created {@code ExecutionEvent}
	 * @see EventType#DYNAMIC_TEST_REGISTERED
	 */
	public static ExecutionEvent dynamicTestRegistered(TestDescriptor testDescriptor) {
		return new ExecutionEvent(EventType.DYNAMIC_TEST_REGISTERED, testDescriptor, null);
	}

	/**
	 * Create a <em>skipped</em> {@code ExecutionEvent} for the supplied
	 * {@link TestDescriptor} and {@code reason}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param reason the reason the execution was skipped; may be {@code null}
	 * @return the newly created {@code ExecutionEvent}
	 * @see EventType#SKIPPED
	 */
	public static ExecutionEvent executionSkipped(TestDescriptor testDescriptor, String reason) {
		return new ExecutionEvent(EventType.SKIPPED, testDescriptor, reason);
	}

	/**
	 * Create a <em>started</em> {@code ExecutionEvent} for the supplied
	 * {@link TestDescriptor}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the
	 * event; never {@code null}
	 * @return the newly created {@code ExecutionEvent}
	 * @see EventType#STARTED
	 */
	public static ExecutionEvent executionStarted(TestDescriptor testDescriptor) {
		return new ExecutionEvent(EventType.STARTED, testDescriptor, null);
	}

	/**
	 * Create a <em>finished</em> {@code ExecutionEvent} for the supplied
	 * {@link TestDescriptor} and {@link TestExecutionResult}.
	 *
	 * @param testDescriptor the {@code TestDescriptor} associated with the
	 * event; never {@code null}
	 * @param result the {@code TestExecutionResult} for the supplied
	 * {@code TestDescriptor}; never {@code null}
	 * @return the newly created {@code ExecutionEvent}
	 * @see EventType#FINISHED
	 */
	public static ExecutionEvent executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		Preconditions.notNull(result, "ExecutionEvent of type FINISHED cannot have a null TestExecutionResult");
		return new ExecutionEvent(EventType.FINISHED, testDescriptor, result);
	}

	// --- Predicates ----------------------------------------------------------

	/**
	 * Create a {@link Predicate} for {@linkplain ExecutionEvent execution events}
	 * whose payload types match the supplied {@code payloadType} and whose payloads
	 * match the supplied {@code payloadPredicate}.
	 *
	 * @param payloadType the required payload type
	 * @param payloadPredicate a {@code Predicate} to match against payloads
	 * @return the resulting {@code Predicate}
	 */
	public static <T> Predicate<ExecutionEvent> byPayload(Class<T> payloadType, Predicate<? super T> payloadPredicate) {
		return event -> event.getPayload(payloadType).filter(payloadPredicate).isPresent();
	}

	/**
	 * Create a {@link Predicate} for {@linkplain ExecutionEvent execution events}
	 * whose {@linkplain EventType event types} match the supplied {@code type}.
	 *
	 * @param type the type to match against
	 * @return the resulting {@code Predicate}
	 */
	public static Predicate<ExecutionEvent> byType(EventType type) {
		return event -> event.type.equals(type);
	}

	/**
	 * Create a {@link Predicate} for {@linkplain ExecutionEvent execution events}
	 * whose {@link TestDescriptor TestDescriptors} match the supplied
	 * {@code testDescriptorPredicate}.
	 *
	 * @param testDescriptorPredicate a {@code Predicate} to match against test
	 * descriptors
	 * @return the resulting {@link Predicate}
	 */
	public static Predicate<ExecutionEvent> byTestDescriptor(
			Predicate<? super TestDescriptor> testDescriptorPredicate) {

		return where(ExecutionEvent::getTestDescriptor, testDescriptorPredicate);
	}

	// -------------------------------------------------------------------------

	private final Instant timestamp = Instant.now();
	private final EventType type;
	private final TestDescriptor testDescriptor;
	private final Object payload;

	/**
	 * Construct an {@code ExecutionEvent} with the supplied arguments.
	 *
	 * @param type the type of the event; never {@code null}
	 * @param testDescriptor the {@code TestDescriptor} associated with the event;
	 * never {@code null}
	 * @param payload the generic payload associated with the event; may be {@code null}
	 */
	private ExecutionEvent(EventType type, TestDescriptor testDescriptor, Object payload) {
		this.type = Preconditions.notNull(type, "EventType must not be null");
		this.testDescriptor = Preconditions.notNull(testDescriptor, "TestDescriptor must not be null");
		this.payload = payload;
	}

	/**
	 * Get the type of this {@code ExecutionEvent}.
	 *
	 * @return the event type; never {@code null}
	 * @see EventType
	 */
	public EventType getType() {
		return this.type;
	}

	/**
	 * Get the {@link TestDescriptor} associated with this {@code ExecutionEvent}.
	 *
	 * @return the {@code TestDescriptor}; never {@code null}
	 */
	public TestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	/**
	 * Get the {@link Instant} when this {@code ExecutionEvent} occurred.
	 *
	 * @return the {@code Instant} when this {@code ExecutionEvent} occurred;
	 * never {@code null}
	 */
	public Instant getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Get the payload, if available.
	 *
	 * @return an {@code Optional} containing the payload; never {@code null}
	 * but potentially empty
	 * @see #getPayload(Class)
	 * @see #getRequiredPayload(Class)
	 */
	public Optional<Object> getPayload() {
		return Optional.ofNullable(this.payload);
	}

	/**
	 * Get the payload of the expected type, if available.
	 *
	 * <p>This is a convenience method that automatically casts the payload to
	 * the expected type. If the payload is not present or is not of the expected
	 * type, this method will return {@link Optional#empty()}.
	 *
	 * @param payloadType the expected payload type; never {@code null}
	 * @return an {@code Optional} containing the payload; never {@code null}
	 * but potentially empty
	 * @see #getPayload()
	 * @see #getRequiredPayload(Class)
	 */
	public <T> Optional<T> getPayload(Class<T> payloadType) {
		Preconditions.notNull(payloadType, "Payload type must not be null");
		return getPayload().filter(payloadType::isInstance).map(payloadType::cast);
	}

	/**
	 * Get the payload of the required type.
	 *
	 * <p>This is a convenience method that automatically casts the payload to
	 * the required type. If the payload is not present or is not of the expected
	 * type, this method will throw an {@link IllegalArgumentException}.
	 *
	 * @param payloadType the required payload type; never {@code null}
	 * @return the payload
	 * @throws IllegalArgumentException if the payload is of a different type
	 * or is not present
	 * @see #getPayload()
	 * @see #getPayload(Class)
	 */
	public <T> T getRequiredPayload(Class<T> payloadType) throws IllegalArgumentException {
		return getPayload(payloadType).orElseThrow(//
			() -> new IllegalArgumentException(
				"ExecutionEvent does not contain a payload of type " + payloadType.getName()));
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("type", this.type)
				.append("testDescriptor", this.testDescriptor)
				.append("timestamp", this.timestamp)
				.append("payload", this.payload)
				.toString();
		// @formatter:on
	}

}
