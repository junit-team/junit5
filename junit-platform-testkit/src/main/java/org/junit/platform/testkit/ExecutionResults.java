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
import static org.junit.platform.testkit.ExecutionEvent.byTestDescriptor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;

/**
 * {@code ExecutionResults} provides a fluent API for processing the results of
 * executing a test plan on the JUnit Platform for a given
 * {@link org.junit.platform.engine.TestEngine TestEngine}.
 *
 * @since 1.4
 * @see #all()
 * @see #containers()
 * @see #tests()
 * @see ExecutionRecorder
 * @see Events
 * @see Executions
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class ExecutionResults {

	private final Events allEvents;
	private final Events testEvents;
	private final Events containerEvents;

	/**
	 * Construct {@link ExecutionResults} from the supplied list of recorded
	 * {@linkplain ExecutionEvent execution events}.
	 *
	 * @param events the list of execution events; never {@code null} or
	 * containing {@code null} elements
	 */
	ExecutionResults(List<ExecutionEvent> events) {
		Preconditions.notNull(events, "ExecutionEvent list must not be null");
		Preconditions.containsNoNullElements(events, "ExecutionEvent list must not contain null elements");

		this.allEvents = new Events(events, "All");
		this.testEvents = new Events(filterEvents(events, TestDescriptor::isTest), "Test");
		this.containerEvents = new Events(filterEvents(events, TestDescriptor::isContainer), "Container");
	}

	/**
	 * Get all recorded events.
	 */
	public Events all() {
		return this.allEvents;
	}

	/**
	 * Get recorded events for containers.
	 *
	 * <p>In this context, the word "container" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isContainer()}.
	 */
	public Events containers() {
		return this.containerEvents;
	}

	/**
	 * Get recorded events for tests.
	 *
	 * <p>In this context, the word "test" applies to {@link TestDescriptor
	 * TestDescriptors} that return {@code true} from {@link TestDescriptor#isTest()}.
	 */
	public Events tests() {
		return this.testEvents;
	}

	/**
	 * Filter the supplied list of events using the supplied predicate.
	 */
	private static Stream<ExecutionEvent> filterEvents(List<ExecutionEvent> events,
			Predicate<? super TestDescriptor> predicate) {

		return events.stream().filter(byTestDescriptor(predicate));
	}

}
