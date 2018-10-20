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
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Represents the entirety of multiple test or container execution runs.
 *
 * @since 1.4
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class ExecutionResults {

	private final List<ExecutionEvent> executionEvents;
	private final List<Execution> testExecutions;

	/**
	 * Construct an {@link ExecutionResults} given a {@link List} of recorded {@link ExecutionEvent}s.
	 *
	 * @param events the {@link List} of {@link ExecutionEvent}s to use when creating the execution graph, cannot be null
	 */
	ExecutionResults(List<ExecutionEvent> events) {
		Preconditions.notNull(events, "ExecutionEvent list must not be null");
		Preconditions.containsNoNullElements(events, "ExecutionEvent list must not contain null elements");

		this.executionEvents = Collections.unmodifiableList(events);
		// Cache test executions by reading from the full list of events
		this.testExecutions = readTestExecutions(events);
	}

	private static List<Execution> readTestExecutions(List<ExecutionEvent> executionEvents) {
		List<Execution> executions = new ArrayList<>();
		Map<TestDescriptor, Instant> executionStarts = new HashMap<>();
		for (ExecutionEvent executionEvent : executionEvents) {
			if (executionEvent.getTestDescriptor().isTest()) {
				switch (executionEvent.getType()) {
					case STARTED: {
						executionStarts.put(executionEvent.getTestDescriptor(), executionEvent.getTimestamp());
						break;
					}
					case SKIPPED: {
						Instant startInstant = executionStarts.get(executionEvent.getTestDescriptor());
						Execution skippedEvent = Execution.skipped(executionEvent.getTestDescriptor(),
							startInstant != null ? startInstant : executionEvent.getTimestamp(),
							executionEvent.getTimestamp(), executionEvent.getPayloadAs(String.class));
						executions.add(skippedEvent);
						executionStarts.remove(executionEvent.getTestDescriptor());
						break;
					}
					case FINISHED: {
						Execution finishedEvent = Execution.finished(executionEvent.getTestDescriptor(),
							executionStarts.get(executionEvent.getTestDescriptor()), executionEvent.getTimestamp(),
							executionEvent.getPayloadAs(TestExecutionResult.class));
						executions.add(finishedEvent);
						executionStarts.remove(executionEvent.getTestDescriptor());
						break;
					}
					default: {
						// Fall through and ignore reporting entry publish and dynamic test registration events
						break;
					}
				}
			}
		}
		return Collections.unmodifiableList(executions);
	}

	// --- ALL Execution Events ------------------------------------------------

	/**
	 * Get all {@link ExecutionEvent}s contained in this {@link ExecutionResults}.
	 *
	 * @return the complete {@link List} of {@link ExecutionEvent}s
	 */
	public List<ExecutionEvent> getExecutionEvents() {
		return this.executionEvents;
	}

	/**
	 * Get the count of {@link ExecutionEvent}s contained in this {@link ExecutionResults}.
	 *
	 * @return the count of {@link ExecutionEvent}s
	 */
	public int getExecutionEventsCount() {
		return this.executionEvents.size();
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type}.
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getExecutionEvents(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, ignored -> true).collect(toList());
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type}.
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public int getExecutionEventsCount(ExecutionEvent.Type type) {
		return getExecutionEvents(type).size();
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of {@link ExecutionEvent.Type#FINISHED} type, with the
	 * provided {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that finished with the provided status
	 */
	public List<ExecutionEvent> getExecutionEventsFinished(TestExecutionResult.Status status) {
		Predicate<ExecutionEvent> byPayload = ExecutionEvent.byPayload(TestExecutionResult.class,
			result -> result.getStatus().equals(status));

		return eventsByTypeAndTestDescriptor(ExecutionEvent.Type.FINISHED, ignored -> true) //
				.filter(byPayload).collect(toList());
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of {@link ExecutionEvent.Type#FINISHED} type, with the
	 * provided {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that finished with the provided status
	 */
	public int getExecutionEventsFinishedCount(TestExecutionResult.Status status) {
		return getExecutionEventsFinished(status).size();
	}

	// --- Reporting Entry Publication Execution Events ------------------------

	/**
	 * Get the count of {@link ExecutionEvent.Type#REPORTING_ENTRY_PUBLISHED} events
	 * for this {@code ExecutionResults}.
	 *
	 * @return the count of {@link ExecutionEvent.Type#REPORTING_ENTRY_PUBLISHED}
	 */
	public int getReportingEntryPublicationCount() {
		return getExecutionEvents(ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED).size();
	}

	// --- Dynamic Test Execution Events ---------------------------------------

	/**
	 * Get the count of {@link ExecutionEvent}s of the type {@link ExecutionEvent.Type#DYNAMIC_TEST_REGISTERED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred of type {@link ExecutionEvent.Type#DYNAMIC_TEST_REGISTERED}
	 */
	public int getDynamicTestRegistrationCount() {
		return getExecutionEvents(ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED).size();
	}

	// --- Container Execution Events ------------------------------------------

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public List<ExecutionEvent> getContainerEvents(ExecutionEvent.Type type) {
		return containerEventsByType(type).collect(toList());
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainerEventsCount(ExecutionEvent.Type type) {
		return getContainerEvents(type).size();
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of the type being {@link ExecutionEvent.Type#SKIPPED} where
	 * the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * <p>NOTE: This is the same as calling {@link #getContainerEventsCount(ExecutionEvent.Type)} with the input of {@link ExecutionEvent.Type#SKIPPED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of type {@link ExecutionEvent.Type#SKIPPED}
	 */
	public int getContainersSkippedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.SKIPPED);
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code STARTED}
	 * for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * <p>NOTE: This is the same as calling {@link #getContainerEventsCount(ExecutionEvent.Type)} with the input of {@link ExecutionEvent.Type#STARTED}.
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainersStartedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.STARTED);
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public List<ExecutionEvent> getContainersFinishedEvents(TestExecutionResult.Status status) {
		Preconditions.notNull(status, "TestExecutionResult.Status cannot be null");

		return containerEventsByType(ExecutionEvent.Type.FINISHED)//
				.filter(ExecutionEvent.byPayload(TestExecutionResult.class,
					where(TestExecutionResult::getStatus, isEqual(status))))//
				.collect(toList());
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished.
	 */
	public int getContainersFinishedCount() {
		return getContainerEventsCount(ExecutionEvent.Type.FINISHED);
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a container of the provided type
	 */
	public int getContainersFinishedCount(TestExecutionResult.Status status) {
		return getContainersFinishedEvents(status).size();
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and {@link TestExecutionResult.Status#FAILED} where the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished with a FAILED status.
	 */
	public int getContainersFailedCount() {
		return getContainersFinishedCount(TestExecutionResult.Status.FAILED);
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * and {@link TestExecutionResult.Status#ABORTED} where the {@link ExecutionEvent} was for a container (in other words: {@link TestDescriptor#isContainer()} ()} == {@code true}).
	 *
	 * @return the count of {@link ExecutionEvent}s that occurred for a container that finished with an ABORTED status.
	 */
	public int getContainersAbortedCount() {
		return getContainersFinishedCount(TestExecutionResult.Status.ABORTED);
	}

	private Stream<ExecutionEvent> containerEventsByType(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isContainer);
	}

	// --- Test Execution Events -----------------------------------------------

	public List<ExecutionEvent> getTestEvents() {
		return getExecutionEvents().stream()//
				.filter(ExecutionEvent.byTestDescriptor(TestDescriptor::isTest))//
				.collect(toList());
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getTestEvents(ExecutionEvent.Type type) {
		return testEventsByType(type).collect(toList());
	}

	/**
	 * Get the count of {@link ExecutionEvent}s of the provided {@link ExecutionEvent.Type} where
	 * the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} ()} == {@code true}).
	 *
	 * @param type the {@link ExecutionEvent.Type} to filter, cannot be null
	 * @return the count of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public int getTestEventsCount(ExecutionEvent.Type type) {
		return getTestEvents(type).size();
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code SKIPPED} and the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} == {@code true}).
	 *
	 * <p>NOTE: This is the same as calling {@link #getTestEvents(ExecutionEvent.Type)} with the input {@link ExecutionEvent.Type#SKIPPED}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of type {@link ExecutionEvent.Type#SKIPPED}
	 */
	public List<ExecutionEvent> getTestsSkippedEvents() {
		return getTestEvents(ExecutionEvent.Type.SKIPPED);
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED}
	 * with the provided {@link TestExecutionResult.Status} and the {@link ExecutionEvent} was for a test (in other words: {@link TestDescriptor#isTest()} ()} == {@code true}).
	 *
	 * @param status the provided {@link TestExecutionResult.Status} to filter, cannot be null
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test of the provided type
	 */
	public List<ExecutionEvent> getTestsFinishedEvents(TestExecutionResult.Status status) {
		Preconditions.notNull(status, "TestExecutionResult.Status cannot be null");

		return testEventsByType(ExecutionEvent.Type.FINISHED)//
				.filter(ExecutionEvent.byPayload(TestExecutionResult.class,
					where(TestExecutionResult::getStatus, isEqual(status))))//
				.collect(toList());
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED} for an {@link ExecutionEvent} that was for a test and completed with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * <p>NOTE: This is the same as calling {@link #getExecutionEventsFinished(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test with the finished status of {@link TestExecutionResult.Status#SUCCESSFUL}
	 */
	public List<ExecutionEvent> getTestsSuccessfulEvents() {
		return getTestsFinishedEvents(TestExecutionResult.Status.SUCCESSFUL);
	}

	/**
	 * Get the {@link List} of {@link ExecutionEvent}s of where the {@link ExecutionEvent.Type} is {@code FINISHED} for an {@link ExecutionEvent} that was for a test and completed with the status of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * <p>NOTE: This is the same as calling {@link #getExecutionEventsFinished(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * @return the {@link List} of {@link ExecutionEvent}s that occurred for a test with the finished status of {@link TestExecutionResult.Status#FAILED}
	 */
	public List<ExecutionEvent> getTestsFailedEvents() {
		return getTestsFinishedEvents(TestExecutionResult.Status.FAILED);
	}

	private Stream<ExecutionEvent> testEventsByType(ExecutionEvent.Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isTest);
	}

	// --- Test Executions -----------------------------------------------------

	/**
	 * Get all Test {@link Execution}s contained in this {@link ExecutionResults}.
	 *
	 * @return the complete {@link List} of {@link Execution}s
	 */
	public List<Execution> getTestExecutions() {
		return this.testExecutions;
	}

	/**
	 * Get the count of all Test {@link Execution}s contained in this {@link ExecutionResults}.
	 *
	 * @return the count of all Test {@link Execution}s
	 */
	public int getTestExecutionsCount() {
		return getTestExecutions().size();
	}

	/**
	 * Get the {@link List} of Test {@link Execution}s that were skipped.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were skipped.
	 */
	public List<Execution> getTestsSkippedExecutions() {
		return testExecutionsByTerminationInfo(TerminationInfo::isSkipReason).collect(toList());
	}

	/**
	 * Get the count of Test {@link Execution}s that were skipped.
	 *
	 * @return the count of Test {@link Execution}s that were skipped.
	 */
	public int getTestsSkippedCount() {
		return getTestsSkippedExecutions().size();
	}

	/**
	 * Get the {@link List} of Test {@link Execution}s that were started. This does <b>NOT</b> include tests that were skipped.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were started
	 */
	public List<Execution> getTestsStartedExecutions() {
		return testExecutionsByTerminationInfo(info -> !info.isSkipReason()).collect(toList());
	}

	/**
	 * Get the count of Test {@link Execution}s that were started. This does <b>NOT</b> include tests that were skipped.
	 *
	 * @return the count of Test {@link Execution}s that were started
	 */
	public int getTestsStartedCount() {
		return getTestsStartedExecutions().size();
	}

	/**
	 * Get the {@link List} of Test {@link Execution}s that were finished / completed, which includes tests that were
	 * {@link TestExecutionResult.Status#SUCCESSFUL}, {@link TestExecutionResult.Status#FAILED} or {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the {@link List} of Test {@link Execution}s that were finished
	 */
	public List<Execution> getTestsFinishedExecutions() {
		return testExecutionsByTerminationInfo(TerminationInfo::isExecutionResult).collect(toList());
	}

	/**
	 * Get the count of Test {@link Execution}s that were finished / completed, which includes tests that were
	 * {@link TestExecutionResult.Status#SUCCESSFUL}, {@link TestExecutionResult.Status#FAILED} or {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the count of Test {@link Execution}s that were finished
	 */
	public int getTestsFinishedCount() {
		return getTestsFinishedExecutions().size();
	}

	/**
	 * Get the {@link List} of Test {@link Execution}s that were finished / completed, filtering on the provided
	 * {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter finished {@link TestExecutionResult}s on, cannot be null
	 * @return the {@link List} of Test {@link Execution}s that finished with the provided {@link TestExecutionResult.Status}
	 */
	public List<Execution> getTestsFinishedExecutions(TestExecutionResult.Status status) {
		return testExecutionsByTerminationInfo(TerminationInfo::isExecutionResult).filter(
			execution -> execution.getTerminationInfo().getExecutionResult().getStatus().equals(
				notNull(status, "TestExecutionResult.Status cannot be null"))).collect(toList());
	}

	/**
	 * Get the count of Test {@link Execution}s that were finished / completed, filtering on the provided
	 * {@link TestExecutionResult.Status}.
	 *
	 * @param status the {@link TestExecutionResult.Status} to filter finished {@link TestExecutionResult}s on, cannot be null
	 * @return the count of Test {@link Execution}s that finished with the provided {@link TestExecutionResult.Status}
	 */
	public int getTestsFinishedCount(TestExecutionResult.Status status) {
		return getTestsFinishedExecutions(status).size();
	}

	/**
	 * Get the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * <p>NOTE: This is the same as calling {@link #getTestsFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#SUCCESSFUL}.
	 */
	public int getTestsSuccessfulCount() {
		return getTestsFinishedCount(TestExecutionResult.Status.SUCCESSFUL);
	}

	/**
	 * Get the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * <p>NOTE: This is the same as calling {@link #getTestsFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#FAILED}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#FAILED}.
	 */
	public int getTestsFailedCount() {
		return getTestsFinishedCount(TestExecutionResult.Status.FAILED);
	}

	/**
	 * Get the count of Test {@link Execution}s that were finished / completed, filtering on only those that finished with the status of {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * <p>NOTE: This is the same as calling {@link #getTestsFinishedCount(TestExecutionResult.Status)} with the input of {@link TestExecutionResult.Status#ABORTED}.
	 *
	 * @return the count of Test {@link Execution}s that finished with the status of {@link TestExecutionResult.Status#ABORTED}.
	 */
	public int getTestsAbortedCount() {
		return getTestsFinishedCount(TestExecutionResult.Status.ABORTED);
	}

	private Stream<Execution> testExecutionsByTerminationInfo(Predicate<TerminationInfo> predicate) {
		return this.testExecutions.stream().filter(execution -> predicate.test(execution.getTerminationInfo()));
	}

	// -------------------------------------------------------------------------

	private Stream<ExecutionEvent> eventsByTypeAndTestDescriptor(ExecutionEvent.Type type,
			Predicate<? super TestDescriptor> predicate) {

		Preconditions.notNull(type, "ExecutionEvent.Type cannot be null");
		Preconditions.notNull(predicate, "TestDescriptor Predicate cannot be null");

		return getExecutionEvents().stream()//
				.filter(ExecutionEvent.byType(type))//
				.filter(ExecutionEvent.byTestDescriptor(predicate));
	}

}
