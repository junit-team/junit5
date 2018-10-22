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

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * Enumeration of the different possible {@link ExecutionEvent} types.
 *
 * @since 1.4
 * @see ExecutionEvent
 */
@API(status = EXPERIMENTAL, since = "1.4")
public enum EventType {

	/**
	 * Called when a {@link TestDescriptor} has been dynamically registered.
	 *
	 * @see org.junit.platform.engine.EngineExecutionListener#dynamicTestRegistered(TestDescriptor)
	 */
	DYNAMIC_TEST_REGISTERED,

	/**
	 * Called when the execution of a leaf or subtree of the test tree has been
	 * skipped.
	 *
	 * @see org.junit.platform.engine.EngineExecutionListener#executionSkipped(TestDescriptor, String)
	 */
	SKIPPED,

	/**
	 * Called when the execution of a leaf or subtree of the test tree is about
	 * to be started.
	 *
	 * @see org.junit.platform.engine.EngineExecutionListener#executionStarted(TestDescriptor)
	 */
	STARTED,

	/**
	 * Called when the execution of a leaf or subtree of the test tree has
	 * finished, regardless of the outcome.
	 *
	 * @see org.junit.platform.engine.EngineExecutionListener#executionFinished(TestDescriptor, TestExecutionResult)
	 */
	FINISHED,

	/**
	 * Called when a {@link TestDescriptor} publishes a reporting entry.
	 *
	 * @see org.junit.platform.engine.EngineExecutionListener#reportingEntryPublished(TestDescriptor, ReportEntry)
	 */
	REPORTING_ENTRY_PUBLISHED;

}
