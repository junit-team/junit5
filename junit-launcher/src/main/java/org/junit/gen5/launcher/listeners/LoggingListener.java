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

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

public class LoggingListener implements TestExecutionListener {

	public static LoggingListener forJUL() {
		Logger logger = Logger.getLogger(LoggingListener.class.getName());
		return new LoggingListener((thrown, messageSupplier) -> logger.log(Level.FINE, thrown, messageSupplier));
	}

	private final BiConsumer<Throwable, Supplier<String>> logger;

	public LoggingListener(BiConsumer<Throwable, Supplier<String>> logger) {
		this.logger = logger;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		log("testPlanExecutionStarted: %s", testPlan);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		log("testPlanExecutionFinished: %s", testPlan);
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		log("dynamicTestRegistered: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		log("testStarted: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, String reason) {
		log("testSkipped: %s - %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), reason);
	}

	@Override
	public void testFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		logWithThrowable("testFinished: %s - %s - %s", testExecutionResult.getThrowable().orElse(null),
			testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), testExecutionResult);
	}

	private void log(String message, Object... args) {
		logWithThrowable(message, null, args);
	}

	private void logWithThrowable(String message, Throwable thrown, Object... args) {
		logger.accept(thrown, () -> String.format(message, args));
	}
}
