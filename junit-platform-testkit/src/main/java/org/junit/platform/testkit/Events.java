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
import static org.junit.platform.testkit.ExecutionEvent.byPayload;
import static org.junit.platform.testkit.ExecutionEvent.byType;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Index;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;

/**
 * {@code Events} is a facade that provides a fluent API for working with
 * {@linkplain ExecutionEvent execution events}.
 *
 * @since 1.4
 */
@API(status = EXPERIMENTAL, since = "1.4")
public final class Events {

	private final List<ExecutionEvent> events;
	private final String category;

	Events(Stream<ExecutionEvent> events, String category) {
		this(Preconditions.notNull(events, "ExecutionEvent stream must not be null").collect(toList()), category);
	}

	Events(List<ExecutionEvent> events, String category) {
		Preconditions.notNull(events, "ExecutionEvent list must not be null");
		Preconditions.containsNoNullElements(events, "ExecutionEvent list must not contain null elements");

		this.events = Collections.unmodifiableList(events);
		this.category = category;
	}

	String getCategory() {
		return this.category;
	}

	// --- Accessors -----------------------------------------------------------

	/**
	 * Get the {@linkplain ExecutionEvent execution events} as a {@link List}.
	 *
	 * @return the list of execution events; never {@code null}
	 * @see #stream()
	 */
	public List<ExecutionEvent> list() {
		return this.events;
	}

	/**
	 * Get the {@linkplain ExecutionEvent execution events} as a {@link Stream}.
	 *
	 * @return the stream of execution events; never {@code null}
	 * @see #list()
	 */
	public Stream<ExecutionEvent> stream() {
		return this.events.stream();
	}

	/**
	 * Shortcut for {@code events.stream().map(mapper)}.
	 *
	 * @see #stream()
	 * @see Stream#map(Function)
	 */
	public <R> Stream<R> map(Function<? super ExecutionEvent, ? extends R> mapper) {
		Preconditions.notNull(mapper, "Mapping function must not be null");
		return stream().map(mapper);
	}

	/**
	 * Shortcut for {@code events.stream().filter(predicate)}.
	 *
	 * @see #stream()
	 * @see Stream#filter(Predicate)
	 */
	public Stream<ExecutionEvent> filter(Predicate<? super ExecutionEvent> predicate) {
		Preconditions.notNull(predicate, "Filter predicate must not be null");
		return stream().filter(predicate);
	}

	/**
	 * Get the {@link Executions} for the current set of
	 * {@linkplain ExecutionEvent execution events}.
	 *
	 * @return an instance of {@code Executions} for the current set of events;
	 * never {@code null}
	 */
	public Executions executions() {
		return new Executions(this.events, this.category);
	}

	// --- Statistics ----------------------------------------------------------

	/**
	 * Get the number of {@linkplain ExecutionEvent execution events} contained
	 * in this {@code Events} object.
	 */
	public long count() {
		return this.events.size();
	}

	// --- Built-in Filters ----------------------------------------------------

	/**
	 * Get the skipped {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events skipped() {
		return new Events(eventsByType(EventType.SKIPPED), this.category + " Skipped");
	}

	/**
	 * Get the started {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events started() {
		return new Events(eventsByType(EventType.STARTED), this.category + " Started");
	}

	/**
	 * Get the finished {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events finished() {
		return new Events(eventsByType(EventType.FINISHED), this.category + " Finished");
	}

	/**
	 * Get the aborted {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events aborted() {
		return new Events(finishedEventsByStatus(Status.ABORTED), this.category + " Aborted");
	}

	/**
	 * Get the succeeded {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events succeeded() {
		return new Events(finishedEventsByStatus(Status.SUCCESSFUL), this.category + " Successful");
	}

	/**
	 * Get the failed {@link Events} contained in this {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events failed() {
		return new Events(finishedEventsByStatus(Status.FAILED), this.category + " Failed");
	}

	/**
	 * Get the reporting entry publication {@link Events} contained in this
	 * {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events reportingEntryPublished() {
		return new Events(eventsByType(EventType.REPORTING_ENTRY_PUBLISHED),
			this.category + " Reporting Entry Published");
	}

	/**
	 * Get the dynamic registration {@link Events} contained in this
	 * {@code Events} object.
	 *
	 * @return the filtered {@code Events}; never {@code null}
	 */
	public Events dynamicallyRegistered() {
		return new Events(eventsByType(EventType.DYNAMIC_TEST_REGISTERED), this.category + " Dynamically Registered");
	}

	// --- Assertions ----------------------------------------------------------

	/**
	 * Assert statistics for the {@linkplain ExecutionEvent execution events}
	 * contained in this {@code Events} object.
	 *
	 * <h4>Example</h4>
	 *
	 * <p>{@code events.assertStatistics(stats -> stats.started(1).succeeded(1).failed(0));}
	 *
	 * @param statisticsConsumer a consumer of {@link EventStatistics}
	 */
	public void assertStatistics(Consumer<EventStatistics> statisticsConsumer) {
		EventStatistics eventStatistics = new EventStatistics(this, this.category);
		statisticsConsumer.accept(eventStatistics);
		eventStatistics.assertAll();
	}

	/**
	 * Assert that all {@linkplain ExecutionEvent execution events} contained in
	 * this {@code Events} object exactly match the provided conditions.
	 *
	 * <p>Conditions can be imported statically from {@link ExecutionEventConditions}
	 * and {@link TestExecutionResultConditions}.
	 *
	 * <h4>Example</h4>
	 *
	 * <pre class="code">
	 * executionResults.tests().assertEventsMatchExactly(
	 *     event(test("exampleTestMethod"), started()),
	 *     event(test("exampleTestMethod"), finishedSuccessfully())
	 * );
	 * </pre>
	 *
	 * @param conditions the conditions to match against
	 * @see ExecutionEventConditions
	 * @see TestExecutionResultConditions
	 */
	@SafeVarargs
	public final void assertEventsMatchExactly(Condition<? super ExecutionEvent>... conditions) {
		assertExecutionEventsMatchExactly(this.events, conditions);
	}

	/**
	 * Shortcut for {@code org.assertj.core.api.Assertions.assertThat(events.list())}.
	 *
	 * @return an instance of {@link ListAssert} for execution events; never
	 * {@code null}
	 * @see org.assertj.core.api.Assertions#assertThat(List)
	 * @see org.assertj.core.api.ListAssert
	 */
	public ListAssert<ExecutionEvent> assertThatEvents() {
		return org.assertj.core.api.Assertions.assertThat(list());
	}

	// --- Diagnostics ---------------------------------------------------------

	/**
	 * Print all events to {@link System#out}.
	 *
	 * @return this {@code Events} object for method chaining; never {@code null}
	 */
	public Events debug() {
		debug(System.out);
		return this;
	}

	/**
	 * Print all events to the supplied {@link OutputStream}.
	 *
	 * @return this {@code Events} object for method chaining; never {@code null}
	 */
	public Events debug(OutputStream out) {
		debug(new PrintWriter(out, true));
		return this;
	}

	/**
	 * Print all events to the supplied {@link Writer}.
	 *
	 * @return this {@code Events} object for method chaining; never {@code null}
	 */
	public Events debug(Writer writer) {
		debug(new PrintWriter(writer, true));
		return this;
	}

	private Events debug(PrintWriter printWriter) {
		printWriter.println(this.category + " Events:");
		this.events.forEach(event -> printWriter.printf("\t%s%n", event));
		return this;
	}

	// --- Internals -----------------------------------------------------------

	private Stream<ExecutionEvent> eventsByType(EventType type) {
		Preconditions.notNull(type, "EventType must not be null");
		return stream().filter(byType(type));
	}

	private Stream<ExecutionEvent> finishedEventsByStatus(Status status) {
		Preconditions.notNull(status, "Status must not be null");
		return eventsByType(EventType.FINISHED)//
				.filter(byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
	}

	@SafeVarargs
	private static void assertExecutionEventsMatchExactly(List<ExecutionEvent> executionEvents,
			Condition<? super ExecutionEvent>... conditions) {

		Assertions.assertThat(executionEvents).hasSize(conditions.length);

		SoftAssertions softly = new SoftAssertions();
		for (int i = 0; i < conditions.length; i++) {
			softly.assertThat(executionEvents).has(conditions[i], Index.atIndex(i));
		}
		softly.assertAll();
	}

}
