/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;
import org.junit.gen5.launcher.TestExecutionListener;

/**
 * Simple {@link TestExecutionListener} that tracks the number of times that certain callbacks are invoked.
 *
 * @since 5.0
 */
public class TrackingEngineExecutionListener implements EngineExecutionListener {

	public final AtomicInteger testStartedCount = new AtomicInteger();
	public final AtomicInteger testSucceededCount = new AtomicInteger();
	public final AtomicInteger testSkippedCount = new AtomicInteger();
	public final AtomicInteger testAbortedCount = new AtomicInteger();
	public final AtomicInteger testFailedCount = new AtomicInteger();

	public final AtomicInteger containerStartedCount = new AtomicInteger();
	public final AtomicInteger containerFinishedCount = new AtomicInteger();
	public final AtomicInteger containerSkippedCount = new AtomicInteger();

	public final List<Throwable> throwables = new ArrayList<>();

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		// no-op
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		if (testDescriptor.isTest()) {
			testStartedCount.incrementAndGet();
		}
		else {
			containerStartedCount.incrementAndGet();
		}
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		if (testDescriptor.isTest()) {
			testSkippedCount.incrementAndGet();
		}
		else {
			containerSkippedCount.incrementAndGet();
		}
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		if (testDescriptor.isTest()) {
			getCounter(testExecutionResult.getStatus()).incrementAndGet();
		}
		else {
			containerFinishedCount.incrementAndGet();
		}
		testExecutionResult.getThrowable().ifPresent(throwables::add);
	}

	private AtomicInteger getCounter(Status status) {
		switch (status) {
			case SUCCESSFUL:
				return testSucceededCount;
			case ABORTED:
				return testAbortedCount;
			case FAILED:
				return testFailedCount;
			default:
				throw new IllegalArgumentException("Unknown status: " + status);
		}
	}

}
