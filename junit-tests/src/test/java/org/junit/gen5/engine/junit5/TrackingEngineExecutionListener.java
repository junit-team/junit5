/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;
import org.junit.gen5.launcher.TestExecutionListener;

/**
 * Simple {@link TestExecutionListener} that tracks the number of times
 * that certain callbacks are invoked.
 *
 * @since 5.0
 */
public class TrackingEngineExecutionListener implements EngineExecutionListener {

	public final AtomicInteger testStartedCount = new AtomicInteger();
	public final AtomicInteger testSucceededCount = new AtomicInteger();
	public final AtomicInteger testSkippedCount = new AtomicInteger();
	public final AtomicInteger testAbortedCount = new AtomicInteger();
	public final AtomicInteger testFailedCount = new AtomicInteger();

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		// no-op
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		testStartedCount.incrementAndGet();
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, String reason) {
		testSkippedCount.incrementAndGet();
	}

	@Override
	public void testFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		getCounter(testExecutionResult.getStatus()).incrementAndGet();

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
