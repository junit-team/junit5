/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;

/**
 * {@code EngineExecutionResults} provides a fluent API for processing the
 * results of executing a test plan on the JUnit Platform for a given
 * {@link org.junit.platform.engine.TestEngine TestEngine}.
 *
 * @since 1.4
 * @see #allEvents()
 * @see #containerEvents()
 * @see #testEvents()
 * @see ExecutionRecorder
 * @see Events
 * @see Executions
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class EngineExecutionResults {

	private final Events allEvents;
	private final Events testEvents;
	private final Events containerEvents;

	/**
	 * Construct {@link EngineExecutionResults} from the supplied list of recorded
	 * {@linkplain Event events}.
	 *
	 * @param events the list of events; never {@code null} or
	 * containing {@code null} elements
	 */
	EngineExecutionResults(List<Event> events) {
		Preconditions.notNull(events, "Event list must not be null");
		Preconditions.containsNoNullElements(events, "Event list must not contain null elements");

		this.allEvents = new Events(events, "All");
		this.testEvents = new Events(filterEvents(events, TestDescriptor::isTest), "Test");
		this.containerEvents = new Events(filterEvents(events, TestDescriptor::isContainer), "Container");
	}

	/**
	 * Get all recorded events.
	 *
	 * @since 1.6
	 * @see #containerEvents()
	 * @see #testEvents()
	 */
	@API(status = EXPERIMENTAL, since = "1.6")
	public Events allEvents() {
		return this.allEvents;
	}

	/**
	 * Get all recorded events.
	 *
	 * @deprecated This method will be removed in 1.7; use {@link #allEvents()} instead.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.6")
	public Events all() {
		return allEvents();
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isContainer()}.
	 *
	 * @since 1.6
	 * @see #allEvents()
	 * @see #testEvents()
	 */
	@API(status = EXPERIMENTAL, since = "1.6")
	public Events containerEvents() {
		return this.containerEvents;
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isContainer()}.
	 *
	 * @deprecated This method will be removed in 1.7; use {@link #containerEvents()} instead.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.6")
	public Events containers() {
		return containerEvents();
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isTest()}.
	 *
	 * @since 1.6
	 * @see #allEvents()
	 * @see #containerEvents()
	 */
	@API(status = EXPERIMENTAL, since = "1.6")
	public Events testEvents() {
		return this.testEvents;
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isTest()}.
	 *
	 * @deprecated This method will be removed in 1.7; use {@link #testEvents()} instead.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.6")
	public Events tests() {
		return testEvents();
	}

	/**
	 * Filter the supplied list of events using the supplied predicate.
	 */
	private static Stream<Event> filterEvents(List<Event> events, Predicate<? super TestDescriptor> predicate) {
		return events.stream().filter(byTestDescriptor(predicate));
	}

}
