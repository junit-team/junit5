/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import java.util.function.Consumer;
import java.util.logging.Logger;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

public class LoggingListener implements TestExecutionListener {

	private static final Logger LOG = Logger.getLogger(LoggingListener.class.getName());

	private Consumer<String> logger = (aString -> LOG.fine(() -> aString));

	private void log(String logString, Object... args) {
		logger.accept("> " + String.format(logString, args));
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		log("testPlanExecutionStarted: %s", testPlan);
	}

	@Override
	public void testPlanExecutionPaused(TestPlan testPlan) {
		log("testPlanExecutionPaused: %s", testPlan);
	}

	@Override
	public void testPlanExecutionRestarted(TestPlan testPlan) {
		log("testPlanExecutionRestarted: %s", testPlan);
	}

	@Override
	public void testPlanExecutionStopped(TestPlan testPlan) {
		log("testPlanExecutionStopped: %s", testPlan);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		log("testPlanExecutionFinished: %s", testPlan);
	}

	@Override
	public void testPlanExecutionStartedOnEngine(TestPlan testPlan, TestEngine testEngine) {
		log("testPlanExecutionStartedOnEngine: %s %s", testPlan, testEngine);
	}

	@Override
	public void testPlanExecutionFinishedOnEngine(TestPlan testPlan, TestEngine testEngine) {
		log("testPlanExecutionFinishedOnEngine: %s %s", testPlan, testEngine);
	}

	@Override
	public void dynamicTestFound(TestIdentifier testIdentifier) {
		log("dynamicTestFound: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		log("testStarted: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
		log("testSkipped: %s - %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), t.getMessage());
	}

	@Override
	public void testAborted(TestIdentifier testIdentifier, Throwable t) {
		log("testAborted: %s - %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), t.getMessage());
	}

	@Override
	public void testFailed(TestIdentifier testIdentifier, Throwable t) {
		log("testFailed: %s - %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), t.getMessage());
	}

	@Override
	public void testSucceeded(TestIdentifier testIdentifier) {
		log("testSucceeded: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}
}
