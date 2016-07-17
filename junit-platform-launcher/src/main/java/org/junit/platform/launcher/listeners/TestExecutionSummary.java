/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.listeners;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.PrintWriter;

import org.junit.platform.commons.meta.API;

/**
 * Summary of test plan execution.
 *
 * @since 1.0
 * @see SummaryGeneratingListener
 */
@API(Experimental)
public interface TestExecutionSummary {

	/**
	 * Get the number of tests found.
	 */
	long getTestsFoundCount();

	/**
	 * Get the number of tests started.
	 */
	long getTestsStartedCount();

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
	 *
	 * @see #getContainersFailedCount()
	 * @see #getTotalFailureCount()
	 */
	long getTestsFailedCount();

	/**
	 * Get the number of containers that failed.
	 *
	 * @see #getTestsFailedCount()
	 * @see #getTotalFailureCount()
	 */
	long getContainersFailedCount();

	/**
	 * Get the total number of {@linkplain #getContainersFailedCount failed
	 * containers} and {@linkplain #getTestsFailedCount failed tests}.
	 *
	 * @see #getTestsFailedCount()
	 * @see #getContainersFailedCount()
	 */
	long getTotalFailureCount();

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
	 * Print failed containers and tests, including sources and exception
	 * messages, to the supplied {@link PrintWriter}.
	 *
	 * @see #printTo(PrintWriter)
	 */
	void printFailuresTo(PrintWriter writer);

}
