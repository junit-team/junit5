/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestSummaryReportingTestListener implements TestPlanExecutionListener, TestExecutionListener {

	private final PrintStream out;

	private final AtomicInteger testsStarted = new AtomicInteger();
	private final AtomicInteger testsFound = new AtomicInteger();
	private final AtomicInteger testsSkipped = new AtomicInteger();
	private final AtomicInteger testsAborted = new AtomicInteger();
	private final AtomicInteger testsSucceeded = new AtomicInteger();
	private final AtomicInteger testsFailed = new AtomicInteger();

	private long timeStarted;
	private long timePaused;
	private long timeFinished;


	public TestSummaryReportingTestListener(PrintStream out) {
		this.out = out;
	}

	@Override
	public void testPlanExecutionStarted(int numberOfStaticTests) {
		this.testsFound.set(numberOfStaticTests);
		this.timeStarted = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionPaused() {
		this.timePaused = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionRestarted() {
		this.timeStarted += System.currentTimeMillis() - this.timePaused;
		this.timePaused = 0;
	}

	@Override
	public void testPlanExecutionStopped() {
		reportSummary("Test run stopped");
	}

	@Override
	public void testPlanExecutionFinished() {
		reportSummary("Test run finished");
	}

	private void reportSummary(String msg) {
		this.timeFinished = System.currentTimeMillis();

		// @formatter:off
		out.println(String.format(
			"%s after %d ms\n"
			+ "[%10d tests found     ]\n"
			+ "[%10d tests started   ]\n"
			+ "[%10d tests skipped   ]\n"
			+ "[%10d tests aborted   ]\n"
			+ "[%10d tests failed    ]\n"
			+ "[%10d tests successful]\n",
			msg, (this.timeFinished - this.timeStarted), this.testsFound.get(), this.testsStarted.get(),
			this.testsSkipped.get(), this.testsAborted.get(), this.testsFailed.get(), this.testsSucceeded.get()));
		// @formatter:on
	}

	@Override
	public void dynamicTestFound(TestDescriptor testDescriptor) {
		this.testsFound.incrementAndGet();
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		this.testsStarted.incrementAndGet();
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		this.testsSkipped.incrementAndGet();
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		this.testsAborted.incrementAndGet();
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		this.testsFailed.incrementAndGet();
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		this.testsSucceeded.incrementAndGet();
	}

}
