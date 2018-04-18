/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.test;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.test.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.engine.test.ExecutionEvent.Type.FINISHED;
import static org.junit.platform.engine.test.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.platform.engine.test.ExecutionEvent.Type.SKIPPED;
import static org.junit.platform.engine.test.ExecutionEvent.Type.STARTED;
import static org.junit.platform.engine.test.ExecutionEvent.byPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

@API(status = API.Status.EXPERIMENTAL, since = "1.1.1")
public class ExecutionGraph {

	private List<ExecutionEvent> events;

	private ExecutionGraph(List<ExecutionEvent> events) {
		this.events = events;
	}

	public static Builder builder() {
		return new Builder();
	}

	public long getReportingEntryPublishedCount() {
		return testEventsByType(REPORTING_ENTRY_PUBLISHED).count();
	}

	public long getDynamicTestRegisteredCount() {
		return eventsByTypeAndTestDescriptor(DYNAMIC_TEST_REGISTERED, descriptor -> true).count();
	}

	public long getTestSkippedCount() {
		return testEventsByType(SKIPPED).count();
	}

	public long getTestStartedCount() {
		return testEventsByType(STARTED).count();
	}

	public long getTestFinishedCount() {
		return testEventsByType(FINISHED).count();
	}

	public long getTestSuccessfulCount() {
		return getTestFinishedCount(TestExecutionResult.Status.SUCCESSFUL);
	}

	public long getTestAbortedCount() {
		return getTestFinishedCount(TestExecutionResult.Status.ABORTED);
	}

	public long getTestFailedCount() {
		return getTestFinishedCount(TestExecutionResult.Status.FAILED);
	}

	public long getContainerSkippedCount() {
		return containerEventsByType(SKIPPED).count();
	}

	public long getContainerStartedCount() {
		return containerEventsByType(STARTED).count();
	}

	public long getContainerFinishedCount() {
		return containerEventsByType(FINISHED).count();
	}

	public long getContainerFailedCount() {
		return getContainerFinishedCount(TestExecutionResult.Status.FAILED);
	}

	private long getTestFinishedCount(TestExecutionResult.Status status) {
		return testFinishedEvents(status).count();
	}

	private long getContainerFinishedCount(TestExecutionResult.Status status) {
		return containerFinishedEvents(status).count();
	}

	public List<ExecutionEvent> getSkippedTestEvents() {
		return testEventsByType(SKIPPED).collect(toList());
	}

	public List<ExecutionEvent> getSuccessfulTestFinishedEvents() {
		return testFinishedEvents(TestExecutionResult.Status.SUCCESSFUL).collect(toList());
	}

	public List<ExecutionEvent> getFailedTestFinishedEvents() {
		return testFinishedEvents(TestExecutionResult.Status.FAILED).collect(toList());
	}

	public List<ExecutionEvent> getFailedContainerFinishedEvents() {
		return containerFinishedEvents(TestExecutionResult.Status.FAILED).collect(toList());
	}

	private Stream<ExecutionEvent> containerFinishedEvents(TestExecutionResult.Status status) {
		return containerEventsByType(FINISHED).filter(
			byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
	}

	private Stream<ExecutionEvent> testFinishedEvents(TestExecutionResult.Status status) {
		return testEventsByType(FINISHED).filter(
			byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
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
