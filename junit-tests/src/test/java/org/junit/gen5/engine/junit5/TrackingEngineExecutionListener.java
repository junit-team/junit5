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
	public void testStarted(TestDescriptor testDescriptor) {
		testStartedCount.incrementAndGet();
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		testSucceededCount.incrementAndGet();
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		testSkippedCount.incrementAndGet();
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		testAbortedCount.incrementAndGet();
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		testFailedCount.incrementAndGet();
	}

}
