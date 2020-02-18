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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.started;

import java.util.List;

import org.assertj.core.error.AssertJMultipleFailuresError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

class EventsTests {

	TestDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("e1"), "engine");

	List<Event> list = List.of(Event.executionStarted(engineDescriptor),
		Event.executionSkipped(engineDescriptor, "reason"),
		Event.executionFinished(engineDescriptor, TestExecutionResult.successful()));

	Events events = new Events(list, "test");

	@Test
	void assertEventsMatchExactlyMatchesAllEventsInOrder() {
		events.assertEventsMatchExactly( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesAllEventsInOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesAllEventsInWrongOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), finishedSuccessfully()), //
			event(engine(), skippedWithReason("reason")), //
			event(engine(), started()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesATailingSubset() {
		events.assertEventsMatchLoosely( //
			event(engine(), skippedWithReason("reason")), //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesAStartingSubset() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()), //
			event(engine(), skippedWithReason("reason")) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesASubsetInWrongOrder() {
		events.assertEventsMatchLoosely( //
			event(engine(), skippedWithReason("reason")), //
			event(engine(), started()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesTheLastEventAlone() {
		events.assertEventsMatchLoosely( //
			event(engine(), finishedSuccessfully()) //
		);
	}

	@Test
	void assertEventsMatchLooselyMatchesTheFirstEventAlone() {
		events.assertEventsMatchLoosely( //
			event(engine(), started()) //
		);
	}

	@Test
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

}
