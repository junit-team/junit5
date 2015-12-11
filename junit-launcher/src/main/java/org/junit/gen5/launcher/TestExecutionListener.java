/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

/**
 * @since 5.0
 */
public interface TestExecutionListener {

	default void testPlanExecutionStarted(TestPlan testPlan) {
	}

	default void testPlanExecutionFinished(TestPlan testPlan) {
	}

	default void dynamicTestRegistered(TestIdentifier testIdentifier) {
	}

	default void testStarted(TestIdentifier testIdentifier) {
	}

	default void testSkipped(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testAborted(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testFailed(TestIdentifier testIdentifier, Throwable t) {
	}

	default void testSucceeded(TestIdentifier testIdentifier) {
	}
}