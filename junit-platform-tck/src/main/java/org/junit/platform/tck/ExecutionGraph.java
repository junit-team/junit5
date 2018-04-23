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

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.FunctionUtils.where;

@API(status = API.Status.EXPERIMENTAL, since = "1.2.0")
public class ExecutionGraph {

	private List<ExecutionEvent> events;
	private List<TestExecution> testExecutions;

	private ExecutionGraph(List<ExecutionEvent> events) {
		this.events = events;
		this.testExecutions = readTestExecutions(events);
	}

	private static List<TestExecution> readTestExecutions(List<ExecutionEvent> executionEvents) {
		List<TestExecution> executions = new ArrayList<>();
		Map<TestDescriptor, LocalDateTime> executionStarts = new HashMap<>();
		for (ExecutionEvent executionEvent : executionEvents) {
			if (executionEvent.getTestDescriptor().isTest()) {
				switch (executionEvent.getType()) {
					case STARTED:
						executionStarts.put(executionEvent.getTestDescriptor(), executionEvent.getDateTimeOccurred());
						continue;
					case SKIPPED:
						executions.add(TestExecution.skipped(executionEvent.getTestDescriptor(),
							executionStarts.get(executionEvent.getTestDescriptor()),
							executionEvent.getDateTimeOccurred(), executionEvent.getPayloadAs(String.class)));
						executionStarts.remove(executionEvent.getTestDescriptor());
						continue;
					case FINISHED:
						executions.add(TestExecution.executed(executionEvent.getTestDescriptor(),
							executionStarts.get(executionEvent.getTestDescriptor()),
							executionEvent.getDateTimeOccurred(),
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

	public static Builder builder() {
		return new Builder();
	}

	public long getReportingEntryPublishedCount() {
		return testEventsByType(ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED).count();
	}

	public long getDynamicTestRegisteredCount() {
		return eventsByTypeAndTestDescriptor(ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED, descriptor -> true).count();
	}

	public List<TestExecution> getTestExecutionsSkipped() {
		return testExecutions.stream().filter(
			testExecution -> testExecution.getTerminationInfo().isSkipReason()).collect(toList());
	}

	public List<TestExecution> getTestExecutionsFinished() {
		return testExecutions.stream().filter(
			testExecution -> testExecution.getTerminationInfo().isExecutionResult()).collect(toList());
	}

	public List<TestExecution> getTestExecutionsFinished(TestExecutionResult.Status status) {
		return testExecutions.stream().filter(
			testExecution -> testExecution.getTerminationInfo().isExecutionResult()).filter(
				testExecution -> testExecution.getTerminationInfo().getExecutionResult().getStatus().equals(
					status)).collect(toList());
	}

	public long getContainerSkippedCount() {
		return containerEventsByType(ExecutionEvent.Type.SKIPPED).count();
	}

	public long getContainerStartedCount() {
		return containerEventsByType(ExecutionEvent.Type.STARTED).count();
	}

	public long getContainerFinishedCount() {
		return containerEventsByType(ExecutionEvent.Type.FINISHED).count();
	}

	public long getContainerFailedCount() {
		return getContainerFinishedCount(TestExecutionResult.Status.FAILED);
	}

	private long getContainerFinishedCount(TestExecutionResult.Status status) {
		return containerFinishedEvents(status).count();
	}

	public List<ExecutionEvent> getFailedContainerFinishedEvents() {
		return containerFinishedEvents(TestExecutionResult.Status.FAILED).collect(toList());
	}

	private Stream<ExecutionEvent> testFinishedEvents(TestExecutionResult.Status status) {
		return testEventsByType(ExecutionEvent.Type.FINISHED).filter(ExecutionEvent.byPayload(TestExecutionResult.class,
			where(TestExecutionResult::getStatus, isEqual(status))));
	}

	private Stream<ExecutionEvent> containerFinishedEvents(TestExecutionResult.Status status) {
		return containerEventsByType(ExecutionEvent.Type.FINISHED).filter(ExecutionEvent.byPayload(
			TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
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
			ExecutionEvent.byType(type).and(ExecutionEvent.byTestDescriptor(predicate)));
	}

	public List<ExecutionEvent> getExecutionEvents() {
		return events;
	}

	public List<TestExecution> getTestExecutions() {
		return testExecutions;
	}

	public List<ExecutionEvent> getTestFinishedEvents(TestExecutionResult.Status status) {
		return testFinishedEvents(status).collect(toList());
	}

	public static class Builder {
		private List<ExecutionEvent> events = new ArrayList<>();

		public void addEvent(ExecutionEvent event) {
			this.events.add(event);
		}

		public ExecutionGraph build() {
			return new ExecutionGraph(Collections.unmodifiableList(events));
		}
	}

}
