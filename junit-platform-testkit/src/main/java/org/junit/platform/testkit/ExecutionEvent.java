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

import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * Represents a single event fired during execution of the testing lifecycle.
 *
 * @see ExecutionEvent.Type
 * @since 1.4.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.4.0")
public class ExecutionEvent {

	private static final Supplier<IllegalArgumentException> EXCEPTION_NO_PAYLOAD = () -> new IllegalArgumentException(
		"Cannot access payload from ExecutionEvent when no payload is present.");

	private Type type;
	private TestDescriptor testDescriptor;
	private Object payload;
	private Instant timestamp;

	/**
	 * Constructs a new {@link ExecutionEvent} with the provided parameters.
	 *
	 * @param type the {@link ExecutionEvent.Type} of the {@link ExecutionEvent} to create, cannot be null
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @param payload the generic {@link Object} payload that the {@link ExecutionEvent} contains, may be null
	 */
	private ExecutionEvent(ExecutionEvent.Type type, TestDescriptor testDescriptor, Object payload) {
		this.type = notNull(type, "ExecutionEvent must have a Type");
		this.testDescriptor = notNull(testDescriptor, "ExecutionEvent must have a TestDescriptor");
		this.payload = payload;
		this.timestamp = Instant.now();
	}

	/**
	 * Construct a new {@link ExecutionEvent} of type: {@link ExecutionEvent.Type#REPORTING_ENTRY_PUBLISHED}.
	 *
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @param entry the {@link ReportEntry} that was published, cannot be null
	 * @return the newly created {@link ExecutionEvent}
	 */
	public static ExecutionEvent reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		return new ExecutionEvent(Type.REPORTING_ENTRY_PUBLISHED, testDescriptor,
			notNull(entry, "ExecutionEvents of type REPORTING_ENTRY_PUBLISHED, cannot have a null ReportEntry"));
	}

	/**
	 * Construct a new {@link ExecutionEvent} of type: {@link ExecutionEvent.Type#DYNAMIC_TEST_REGISTERED}.
	 *
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @return the newly created {@link ExecutionEvent}
	 */
	public static ExecutionEvent dynamicTestRegistered(TestDescriptor testDescriptor) {
		return new ExecutionEvent(Type.DYNAMIC_TEST_REGISTERED, testDescriptor, null);
	}

	/**
	 * Construct a new {@link ExecutionEvent} of type: {@link ExecutionEvent.Type#SKIPPED}.
	 *
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @param reason the reason {@link String} for why the execution was skipped, cannot be null but may be empty
	 * @return the newly created {@link ExecutionEvent}
	 */
	public static ExecutionEvent executionSkipped(TestDescriptor testDescriptor, String reason) {
		return new ExecutionEvent(Type.SKIPPED, testDescriptor,
			notNull(reason, "ExecutionEvents of type SKIPPED, cannot have a null reason String"));
	}

	/**
	 * Construct a new {@link ExecutionEvent} of type: {@link ExecutionEvent.Type#STARTED}.
	 *
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @return the newly created {@link ExecutionEvent}
	 */
	public static ExecutionEvent executionStarted(TestDescriptor testDescriptor) {
		return new ExecutionEvent(Type.STARTED, testDescriptor, null);
	}

	/**
	 * Construct a new {@link ExecutionEvent} of type: {@link ExecutionEvent.Type#FINISHED}.
	 *
	 * @param testDescriptor the {@link TestDescriptor} of the {@link ExecutionEvent} to create, cannot be null
	 * @param result the {@link TestExecutionResult} of the test finishing, cannot be null
	 * @return the newly created {@link ExecutionEvent}
	 */
	public static ExecutionEvent executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		return new ExecutionEvent(Type.FINISHED, testDescriptor,
			notNull(result, "ExecutionEvents of type FINISHED, cannot have a null TestExecutionResult"));
	}

	/**
	 * Create a composite {@link Predicate} filter for {@link ExecutionEvent}s by {@code payloadClass} matching the
	 * provided {@link Predicate}.
	 *
	 * @param payloadClass the expected payload {@link Class}
	 * @param predicate the {@link Predicate} to match on type {@code T}
	 * @param <T> the input expected payload class type
	 * @return the newly created composite {@link Predicate}
	 */
	public static <T> Predicate<ExecutionEvent> byPayload(Class<T> payloadClass, Predicate<? super T> predicate) {
		return event -> event.getPayload(payloadClass).filter(predicate).isPresent();
	}

	/**
	 * Create a {@link Predicate} filter for {@link ExecutionEvent}s by {@link ExecutionEvent.Type}.
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter
	 * @return the newly created {@link Predicate}
	 */
	public static Predicate<ExecutionEvent> byType(ExecutionEvent.Type type) {
		return event -> event.type.equals(type);
	}

	/**
	 * Create a composite {@link Predicate} filter combining the provided {@link TestDescriptor} {@link Predicate},
	 * to any {@link ExecutionEvent}.
	 *
	 * @param predicate the {@link Predicate} for {@link TestDescriptor}
	 * @return the composite {@link Predicate}
	 */
	public static Predicate<ExecutionEvent> byTestDescriptor(Predicate<? super TestDescriptor> predicate) {
		return where(ExecutionEvent::getTestDescriptor, predicate);
	}

	/**
	 * Gets the {@link ExecutionEvent.Type} of this {@link ExecutionEvent}.
	 *
	 * @return the {@link ExecutionEvent.Type}
	 */
	public ExecutionEvent.Type getType() {
		return type;
	}

	/**
	 * Gets the {@link TestDescriptor} of this {@link ExecutionEvent}, which represents what test or container this
	 * {@link ExecutionEvent} is for.
	 *
	 * @return the {@link TestDescriptor}
	 */
	public TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	/**
	 * Gets the {@link Instant} of when this {@link ExecutionEvent} occurred.
	 *
	 * @return the {@link Instant} of when this {@link ExecutionEvent} occurred
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the {@code payload} in it's {@link Optional} container.
	 *
	 * @return the {@link Optional} containing the {@code payload}
	 */
	public Optional<Object> getPayload() {
		return Optional.ofNullable(payload);
	}

	/**
	 * Gets the {@code payload} of expected type {@code T}, in it's {@link Optional} container.
	 *
	 * <p>This is a convenience method meant for callers who know the expected type of the {@code payload} and
	 * don't want to cast it manually. This means that if the expected type {@code T} of the {@code payload} is
	 * different than the actual type, then an {@link Optional#empty()} will be returned.</p>
	 *
	 * @param payloadClass the expected {@link Class} type of the {@code payload}
	 * @param <T> the input type of the {@code payload}
	 * @return the {@link Optional} containing the expected payload type
	 */
	public <T> Optional<T> getPayload(Class<T> payloadClass) {
		return getPayload().filter(payloadClass::isInstance).map(payloadClass::cast);
	}

	/**
	 * Gets the {@code payload} of expected type {@code T}.
	 * <p>This is a convenience method meant for callers who know that the {@code payload} is present and also know
	 * the actual type of the {@code payload}.  This means that if the expected type {@code T} of the {@code payload}
	 * is different than the actual type <b>OR</b> the {@code payload} isn't present, then an {@link IllegalArgumentException} will be thrown</p>
	 *
	 * @param payloadClass the expected {@link Class} type of the {@code payload}
	 * @param <T> the input type of the {@code payload}
	 * @return the {@code payload} of type {@code T}
	 * @throws IllegalArgumentException if the expected payload is of a different type, or isn't present
	 */
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
				.append("timestamp", timestamp)
				.toString();
		// @formatter:on
	}

	/**
	 * Enumeration of the different possible {@link ExecutionEvent} types.
	 */
	public enum Type {
		/**
		 * Called when a new, dynamic {@link TestDescriptor} has been registered.
		 *
		 * @see org.junit.platform.engine.EngineExecutionListener#dynamicTestRegistered(TestDescriptor)
		 */
		DYNAMIC_TEST_REGISTERED,
		/**
		 * Called when the execution of a leaf or subtree of the test tree has been skipped.
		 *
		 * @see org.junit.platform.engine.EngineExecutionListener#executionSkipped(TestDescriptor, String)
		 */
		SKIPPED,
		/**
		 * Called when the execution of a leaf or subtree of the test tree is about to be started.
		 *
		 * @see org.junit.platform.engine.EngineExecutionListener#executionStarted(TestDescriptor)
		 */
		STARTED,
		/**
		 * Called when the execution of a leaf or subtree of the test tree has finished, regardless of the outcome.
		 *
		 * @see org.junit.platform.engine.EngineExecutionListener#executionFinished(TestDescriptor, TestExecutionResult)
		 */
		FINISHED,
		/**
		 * Called when any {@link TestDescriptor} publishes additional information (ie. Test context or test data).
		 *
		 * @see org.junit.platform.engine.EngineExecutionListener#reportingEntryPublished(TestDescriptor, ReportEntry)
		 */
		REPORTING_ENTRY_PUBLISHED
	}

}
