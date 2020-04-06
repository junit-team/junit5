/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.started;

import java.util.List;

import org.assertj.core.error.AssertJMultipleFailuresError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

class EventsTests {

	TestDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("e1"), "engine");

	List<Event> list = List.of(Event.executionStarted(engineDescriptor),
		Event.executionSkipped(engineDescriptor, "reason1"), Event.executionSkipped(engineDescriptor, "reason2"),
		Event.executionFinished(engineDescriptor, TestExecutionResult.successful()));

	Events events = new Events(list, "test");

	@Test
	@DisplayName("assertEventsMatchExactly: all events in order -> match")
	void assertEventsMatchExactlyMatchesAllEventsInOrder() {
		events.assertEventsMatchExactly( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), skippedWithReason("reason2")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: all events in order -> match")
	void assertEventsMatchLooselyMatchesAllEventsInOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), skippedWithReason("reason2")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: all events in wrong order -> match")
	void assertEventsMatchLooselyMatchesAllEventsInWrongOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), skippedWithReason("reason2")), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), started()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: tailing subset -> match")
	void assertEventsMatchLooselyMatchesATailingSubset() {
		events.assertEventsMatchLoosely( //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: starting subset -> match")
	void assertEventsMatchLooselyMatchesAStartingSubset() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason1")) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: subset in wrong order -> match")
	void assertEventsMatchLooselyMatchesASubsetInWrongOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), started()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: only last event -> match")
	void assertEventsMatchLooselyMatchesTheLastEventAlone() {
		events.assertEventsMatchLoosely( //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: only first event -> match")
	void assertEventsMatchLooselyMatchesTheFirstEventAlone() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: only one bad event -> fails")
	void assertEventsMatchLooselyWithBadConditionsOnlyFails() {
		Executable willFail = () -> events.assertEventsMatchLoosely( //
			event(engine(), finishedWithFailure()), //
			event(engine(), skippedWithReason("other")) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);

		List<Throwable> failures = error.getFailures();
		assertEquals(2, failures.size());
		assertEquals(AssertionError.class, failures.get(0).getClass());
		assertEquals(AssertionError.class, failures.get(1).getClass());
	}

	@Test
	@DisplayName("assertEventsMatchLoosely: one matching and one bad event -> fails")
	void assertEventsMatchLooselyWithOneMatchingAndOneBadConditionFailsPartly() {
		Executable willFail = () -> events.assertEventsMatchLoosely( //
			event(engine(), started()), //
			event(engine(), finishedWithFailure()) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);

		List<Throwable> failures = error.getFailures();
		assertEquals(1, failures.size());
		assertEquals(AssertionError.class, failures.get(0).getClass());
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: all events in order -> match")
	void assertEventsMatchIncompleteButOrderedMatchesAllEventsInOrder() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), skippedWithReason("reason2")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: all events in wrong order -> fail")
	void assertEventsMatchIncompleteButOrderedWithAllEventsInWrongOrderFails() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), skippedWithReason("reason2")), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), started()) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);
		List<Throwable> failures = error.getFailures();
		assertEquals(1, failures.size());
		assertTrue(failures.get(0).getMessage().contains("Conditions are not in the correct order."));
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: tailing subset in order -> match")
	void assertEventsMatchIncompleteButOrderedMatchesATailingSubset() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: starting subset in order -> match")
	void assertEventsMatchIncompleteButOrderedMatchesAStartingSubset() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason1")) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: subset in wrong order -> fail")
	void assertEventsMatchIncompleteButOrderedWithASubsetInWrongOrderFails() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), skippedWithReason("reason1")), //
			event(engine(), started()) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);
		List<Throwable> failures = error.getFailures();
		assertEquals(1, failures.size());
		assertTrue(failures.get(0).getMessage().contains("Conditions are not in the correct order."));
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: last event alone -> match")
	void assertEventsMatchIncompleteButOrderedMatchesTheLastEventAlone() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: first event alone -> match")
	void assertEventsMatchIncompleteButOrderedMatchesTheFirstEventAlone() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), started()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: bad events only -> fail")
	void assertEventsMatchIncompleteButOrderedWithBadConditionsOnlyFails() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), finishedWithFailure()), //
			event(engine(), skippedWithReason("other")) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);

		List<Throwable> failures = error.getFailures();
		assertEquals(2, failures.size());
		assertEquals(AssertionError.class, failures.get(0).getClass());
		assertEquals(AssertionError.class, failures.get(1).getClass());
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: one matching event and one bad event -> fail")
	void assertEventsMatchIncompleteButOrderedWithOneMatchingAndOneBadConditionFailsPartly() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), started()), //
			event(engine(), finishedWithFailure()) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);
		assertEquals(1, error.getFailures().size());
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: first and last event in order -> match")
	void assertEventsMatchIncompleteButOrderedMatchesFirstAndLastEventInOrder() {
		events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), started()), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: second and last event in bad order -> fail")
	void assertEventsMatchIncompleteButOrderedWithSecondAndLastEventInBadOrderFails() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), finishedSuccessfully()), //
			event(engine(), skippedWithReason("reason1")) //
		);

		AssertJMultipleFailuresError error = assertThrows(AssertJMultipleFailuresError.class, willFail);
		List<Throwable> failures = error.getFailures();
		assertEquals(1, failures.size());
		assertTrue(failures.get(0).getMessage().contains("Conditions are not in the correct order."));
	}

	@Test
	@DisplayName("assertEventsMatchIncompleteButOrdered: too many events -> fail")
	void assertEventsMatchIncompleteButOrderedWithTooEventsFails() {
		Executable willFail = () -> events.assertEventsMatchIncompleteButOrdered( //
			event(engine(), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		AssertionError error = assertThrows(AssertionError.class, willFail);
		assertTrue(error.getMessage().endsWith("to be less than or equal to 4 but was 6"));
	}

}
