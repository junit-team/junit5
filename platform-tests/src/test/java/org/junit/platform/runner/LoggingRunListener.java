/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.runner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Logging {@link RunListener} which can be used for manual debugging purposes
 * within the test suite.
 *
 * @since 1.0
 */
class LoggingRunListener extends RunListener {

	@Override
	public void testRunStarted(Description description) throws Exception {
		System.err.println("testRunStarted: " + description);
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		System.err.println("testRunFinished: " + result);
	}

	@Override
	public void testStarted(Description description) throws Exception {
		System.err.println("testStarted: " + description);
	}

	@Override
	public void testFinished(Description description) throws Exception {
		System.err.println("testFinished: " + description);
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		System.err.println("testFailure: " + failure);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		System.err.println("testAssumptionFailure: " + failure);
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		System.err.println("testIgnored: " + description);
	}

}
