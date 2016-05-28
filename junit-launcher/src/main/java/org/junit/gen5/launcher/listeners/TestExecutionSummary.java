/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.PrintWriter;

import org.junit.gen5.commons.meta.API;

/**
 * Summary of test plan execution.
 *
 * @since 5.0
 * @see SummaryGeneratingListener
 */
@API(Experimental)
public interface TestExecutionSummary {

	/**
	 * Get the number of tests started.
	 */
	long getTestsStartedCount();

	/**
	 * Get the number of tests found.
	 */
	long getTestsFoundCount();

	/**
	 * Get the number of tests skipped.
	 */
	long getTestsSkippedCount();

	/**
	 * Get the number of tests aborted.
	 */
	long getTestsAbortedCount();

	/**
	 * Get the number of tests that succeeded.
	 */
	long getTestsSucceededCount();

	/**
	 * Get the number of tests that failed.
	 */
	long getTestsFailedCount();

	/**
	 * Get the timestamp (in milliseconds) when the test plan started.
	 */
	long getTimeStarted();

	/**
	 * Get the timestamp (in milliseconds) when the test plan finished.
	 */
	long getTimeFinished();

	/**
	 * Print this summary to the supplied {@link PrintWriter}.
	 *
	 * <p>This method does not print failure messages.
	 *
	 * @see #printFailuresTo(PrintWriter)
	 */
	void printTo(PrintWriter writer);

	/**
	 * Print failed tests including sources and exception messages to the
	 * supplied {@link PrintWriter}.
	 *
	 * @see #printTo(PrintWriter)
	 */
	void printFailuresTo(PrintWriter writer);

}
