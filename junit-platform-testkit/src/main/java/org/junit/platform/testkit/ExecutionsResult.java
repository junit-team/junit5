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

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Represents the entirety of multiple test or container execution runs.
 *
 * @since 1.4.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.4.0")
public class ExecutionsResult {

	private List<ExecutionEvent> events;
	private List<Execution> executions;

	/**
	 * Construct an {@link ExecutionsResult} given a {@link List} of recorded {@link ExecutionEvent}s.
	 *
	 * @param events the {@link List} of {@link ExecutionEvent}s to use when creating the execution graph, cannot be null.
	 */
	private ExecutionsResult(List<ExecutionEvent> events) {
		this.events = notNull(events, "ExecutionsResult cannot have null input ExecutionEvents");
		// Cache test executions by reading from the full list of events
		this.executions = readTestExecutions(events);
	}

	private static List<Execution> readTestExecutions(List<ExecutionEvent> executionEvents) {
		List<Execution> executions = new ArrayList<>();
		Map<TestDescriptor, Instant> executionStarts = new HashMap<>();
		for (ExecutionEvent executionEvent : executionEvents) {
			if (executionEvent.getTestDescriptor().isTest()) {
				switch (executionEvent.getType()) {
					case STARTED:
						executionStarts.put(executionEvent.getTestDescriptor(), executionEvent.getTimestamp());
						continue;
					case SKIPPED:
						Instant startInstant = executionStarts.get(executionEvent.getTestDescriptor());
						executions.add(Execution.skipped(executionEvent.getTestDescriptor(),
							startInstant != null ? startInstant : executionEvent.getTimestamp(),
							executionEvent.getTimestamp(), executionEvent.getPayloadAs(String.class)));
						executionStarts.remove(executionEvent.getTestDescriptor());
						continue;
					case FINISHED:
						executions.add(Execution.finished(executionEvent.getTestDescriptor(),
							executionStarts.get(executionEvent.getTestDescriptor()), executionEvent.getTimestamp(),
							executionEvent.getPayloadAs(TestExecutionResult.class)));
						executionStarts.remove(executionEvent.getTestDescriptor());
						continue;
					default:
						// Fall through and ignore reporting entry publish + dynamic test register events
				}
			}
		}
		return Collections.unmodifiableList(executions);
	}

	/**
	 * Creates a new {@link ExecutionsResult.Builder} for generating new {@link ExecutionsResult} objects.
	 *
	 * @return the newly created {@link ExecutionsResult.Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Gets all {@link ExecutionEvent}s contained in this {@link ExecutionsResult}.
	 *
	 * @return the complete {@link List} of {@link ExecutionEvent}s
	 */
	public List<ExecutionEvent> getExecutionEvents() {
		return events;
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s contained in this {@link ExecutionsResult}.
	 *
	 * @return the count of {@link ExecutionEvent}s
	 */
	public int getExecutionEventsCount() {
		return events.size();
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type}.
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getExecutionEvents(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, ignored -> true).collect(toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type}.
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public int getExecutionEventsCount(ExecutionEvent.Type type) {
		return getExecutionEvents(type).size();
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of {@link ExecutionEvent.Type#FINISHED} type, with the
	 * provided {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that finished with the provided status
	 */
	public List<ExecutionEvent> getExecutionEventsFinished(TestExecutionResult.Status status) {
		return eventsByTypeAndTestDescriptor(ExecutionEvent.Type.FINISHED, ignored -> true).filter(
			ExecutionEvent.byPayload(TestExecutionResult.class, result -> result.getStatus().equals(status))).collect(
				toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of {@link ExecutionEvent.Type#FINISHED} type, with the
	 * provided {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that finished with the provided status
	 */
	public int getExecutionEventsFinishedCount(TestExecutionResult.Status status) {
		return getExecutionEventsFinished(status).size();
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of the type {@link ExecutionEvent.Type#DYNAMIC_TEST_REGISTERED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred of type {@link ExecutionEvent.Type#DYNAMIC_TEST_REGISTERED}
	 */
	public int getDynamicTestRegisteredCount() {
		return getExecutionEvents(ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED).size();
	}

	public List<ExecutionEvent> getTestEvents() {
		return getExecutionEvents().stream()//
				.filter(ExecutionEvent.byTestDescriptor(TestDescriptor::isTest))//
				.collect(toList());
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getTestEvents(ExecutionEvent.Type type) {
		return testEventsByType(type).collect(toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public int getTestEventsCount(ExecutionEvent.Type type) {
		return getTestEvents(type).size();
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getTestEventsFinished(TestExecutionResult.Status status) {
		return testEventsFinished(status).collect(toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public int getTestEventsFinishedCount(TestExecutionResult.Status status) {
		return getTestEventsFinished(status).size();
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code SKIPPED} and the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} == {@code true}).
	 *
	 * NOTE: This is the same as calling {@link #getTestEvents(ExecutionEvent.Type)} with the input {@link ExecutionEvent.Type#SKIPPED}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of type {@link ExecutionEvent.Type#SKIPPED}
	 */
	public List<ExecutionEvent> getSkippedTestEvents() {
		return getTestEvents(ExecutionEvent.Type.SKIPPED);
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED} for an {@link ExecutionEvent} that was for a test and completed with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * NOTE: This is the same as calling {@link #getExecutionEventsFinished(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test with the finished status of {@link TestExecutionResult.Status#SUCCESSFUL}
	 */
	public List<ExecutionEvent> getSuccessfulTestFinishedEvents() {
		return testEventsFinished(TestExecutionResult.Status.SUCCESSFUL).collect(toList());
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED} for an {@link ExecutionEvent} that was for a test and completed with the status of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * NOTE: This is the same as calling {@link #getExecutionEventsFinished(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test with the finished status of {@link TestExecutionResult.Status#FAILED}
	 */
	public List<ExecutionEvent> getFailedTestFinishedEvents() {
		return testEventsFinished(TestExecutionResult.Status.FAILED).collect(toList());
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public List<ExecutionEvent> getContainerEvents(ExecutionEvent.Type type) {
		return containerEventsByType(type).collect(toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainerEventsCount(ExecutionEvent.Type type) {
		return getContainerEvents(type).size();
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of the type being {@link ExecutionEvent.Type#SKIPPED} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * NOTE: This is the same as calling {@link #getContainerEventsCount(ExecutionEvent.Type)} with the input of {@link ExecutionEvent.Type#SKIPPED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of type {@link ExecutionEvent.Type#SKIPPED}
	 */
	public int getContainerSkippedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.SKIPPED);
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code STARTED}
	 * for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * NOTE: This is the same as calling {@link #getContainerEventsCount(ExecutionEvent.Type)} with the input of {@link ExecutionEvent.Type#STARTED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainerStartedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.STARTED);
	}

	/**
	 * Gets the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public List<ExecutionEvent> getContainerEventsFinished(TestExecutionResult.Status status) {
		return containerEventsFinished(status).collect(toList());
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainerEventsFinishedCount(TestExecutionResult.Status status) {
		return getContainerEventsFinished(status).size();
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished.
	 */
	public int getContainerFinishedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.FINISHED);
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and {@link TestExecutionResult.Status#FAILED} where the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished with a FAILED status.
	 */
	public int getContainerFailedCount() {
		return getContainerEventsFinishedCount(TestExecutionResult.Status.FAILED);
	}

	/**
	 * Gets the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and {@link TestExecutionResult.Status#ABORTED} where the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished with an ABORTED status.
	 */
	public int getContainerAbortedCount() {
		return getContainerEventsFinishedCount(TestExecutionResult.Status.ABORTED);
	}

	/**
	 * Gets all Test {@link Execution}s contained in this {@link ExecutionsResult}.
	 *
	 * @return the complete {@link List} of {@link Execution}s
	 */
	public List<Execution> getTests() {
		return executions;
	}

	/**
	 * Gets the count of all Test {@link Execution}s contained in this {@link ExecutionsResult}.
	 *
	 * @return the count of all Test {@link Execution}s
	 */
	public int getTestCount() {
		return getTests().size();
	}

	/**
	 * Gets the {@link List} of Test {@link Execution}s that were skipped.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were skipped.
	 */
	public List<Execution> getTestSkipped() {
		return executionsByTerminationInfo(TerminationInfo::isSkipReason).collect(toList());
	}

	/**
	 * Gets the count of Test {@link Execution}s that were skipped.
	 *
	 * @return the count of Test {@link Execution}s that were skipped.
	 */
	public int getTestSkippedCount() {
		return getTestSkipped().size();
	}

	/**
	 * Gets the {@link List} of Test {@link Execution}s that were started. This does <b>NOT</b> include tests that were skipped.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were started
	 */
	public List<Execution> getTestStarted() {
		return executionsByTerminationInfo(info -> !info.isSkipReason()).collect(toList());
	}

	/**
	 * Gets the count of Test {@link Execution}s that were started. This does <b>NOT</b> include tests that were skipped.
	 *
	 * @return the count of Test {@link Execution}s that were started
	 */
	public int getTestStartedCount() {
		return getTestStarted().size();
	}

	/**
	 * Gets the {@link List} of Test {@link Execution}s that were finished / completed, which includes tests that were
	 * {@link TestExecutionResult.Status#SUCCESSFUL}, {@link TestExecutionResult.Status#FAILED} or {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were finished
	 */
	public List<Execution> getTestFinished() {
		return executionsByTerminationInfo(TerminationInfo::isExecutionResult).collect(toList());
	}

	/**
	 * Gets the count of Test {@link Execution}s that were finished / completed, which includes tests that were
	 * {@link TestExecutionResult.Status#SUCCESSFUL}, {@link TestExecutionResult.Status#FAILED} or {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the count of Test {@link Execution}s that were finished
	 */
	public int getTestFinishedCount() {
		return getTestFinished().size();
	}

	/**
	 * Gets the {@link List} of Test {@link Execution}s that were finished / completed, filtering on the provided
	 * {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter finished {@link TestExecutionResult}s on, cannot be null
	 * @return the {@link List} of Test {@link Execution}s that finished with the provided {@link TestExecutionResult.Status}
	 */
	public List<Execution> getTestFinished(TestExecutionResult.Status status) {
		return executionsByTerminationInfo(TerminationInfo::isExecutionResult).filter(
			execution -> execution.getTerminationInfo().getExecutionResult().getStatus().equals(
				notNull(status, "TestExecutionResult.Status cannot be null"))).collect(toList());
	}

	/**
	 * Gets the count of Test {@link Execution}s that were finished / completed, filtering on the provided
	 * {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter finished {@link TestExecutionResult}s on, cannot be null
	 * @return the count of Test {@link Execution}s that finished with the provided {@link TestExecutionResult.Status}
	 */
	public int getTestFinishedCount(TestExecutionResult.Status status) {
		return getTestFinished(status).size();
	}

	/**
	 * Gets the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * NOTE: This is the same as calling {@link #getTestFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 */
	public int getTestSuccessfulCount() {
		return getTestFinishedCount(TestExecutionResult.Status.SUCCESSFUL);
	}

	/**
	 * Gets the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * NOTE: This is the same as calling {@link #getTestFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#FAILED}.
	 */
	public int getTestFailedCount() {
		return getTestFinishedCount(TestExecutionResult.Status.FAILED);
	}

	/**
	 * Gets the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * NOTE: This is the same as calling {@link #getTestFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#ABORTED}.
	 */
	public int getTestAbortedCount() {
		return getTestFinishedCount(TestExecutionResult.Status.ABORTED);
	}

	/**
	 * Gets the count of {@link ExecutionEvent.Type#REPORTING_ENTRY_PUBLISHED} for this {@link ExecutionsResult}.
	 *
	 * @return the count of {@link ExecutionEvent.Type#REPORTING_ENTRY_PUBLISHED}
	 */
	public int getReportingEntryPublishedCount() {
		return getTestEvents(ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED).size();
	}

	private Stream<Execution> executionsByTerminationInfo(Predicate<TerminationInfo> predicate) {
		return executions.stream().filter(execution -> predicate.test(execution.getTerminationInfo()));
	}

	private Stream<ExecutionEvent> testEventsFinished(TestExecutionResult.Status status) {
		return testEventsByType(ExecutionEvent.Type.FINISHED).filter(
			ExecutionEvent.byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus,
				isEqual(notNull(status, "TestExecutionResult.Status cannot be null")))));
	}

	private Stream<ExecutionEvent> containerEventsFinished(TestExecutionResult.Status status) {
		return containerEventsByType(ExecutionEvent.Type.FINISHED).filter(
			ExecutionEvent.byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus,
				isEqual(notNull(status, "TestExecutionResult.Status cannot be null")))));
	}

	private Stream<ExecutionEvent> testEventsByType(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isTest);
	}

	private Stream<ExecutionEvent> containerEventsByType(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isContainer);
	}

	private Stream<ExecutionEvent> eventsByTypeAndTestDescriptor(ExecutionEvent.Type type,
			Predicate<? super TestDescriptor> predicate) {
		return getExecutionEvents().stream().filter(
			ExecutionEvent.byType(notNull(type, "ExecutionEvent.Type cannot be null")).and(
				ExecutionEvent.byTestDescriptor(notNull(predicate, "TestDescriptor Predicate cannot be null"))));
	}

	public static class Builder {

		private static final String NULL_INPUT_ERROR = "Provided ExecutionEvents cannot be null";

		private final List<ExecutionEvent> events = new CopyOnWriteArrayList<>();

		/**
		 * Adds one or more {@link ExecutionEvent}s to be used when creating a new {@link ExecutionsResult}.
		 *
		 * @param event the {@link ExecutionEvent} to add, cannot be null
		 * @param events any other {@link ExecutionEvent} to add, cannot be null
		 * @return the {@link Builder}, for method chaining
		 */
		public Builder addEvent(ExecutionEvent event, ExecutionEvent... events) {
			this.events.add(notNull(event, NULL_INPUT_ERROR));
			this.events.addAll(Arrays.asList(notNull(events, NULL_INPUT_ERROR)));
			return this;
		}

		/**
		 * Constructs a new {@link ExecutionsResult} from this {@link Builder}.
		 *
		 * @return the newly created {@link ExecutionsResult}
		 */
		public ExecutionsResult build() {
			return new ExecutionsResult(Collections.unmodifiableList(events));
		}
	}

}
