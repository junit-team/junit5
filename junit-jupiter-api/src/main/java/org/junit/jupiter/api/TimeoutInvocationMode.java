/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
// CS304 Issue link: https://github.com/junit-team/junit5/issues/2087
/**
 * Enumeration of invocation modes for a {@code Timeout}.
 *
 * <p>When a test can stuck in infinite loop it might be useful in
 * some cases to be able to run the test code in a separate thread. In this case
 * this test will be reported as failed, when running test code in the same thread will
 * wait indefinitely for the test code completion. This behaviour occurs because thread
 * stop is used with {@link Thread#interrupt()}. If code in the test ignores interrupts it
 * will run indefinitely.
 *
 * @since 5.9
 * @see Timeout
 */
@API(status = EXPERIMENTAL, since = "5.9")
public enum TimeoutInvocationMode {

	/**
	 * Defer to the configured timeout invocation mode.
	 */
	INFERRED,

	/**
	 * Execute a test code in same thread as the JUnit execution.
	 */
	SAME_THREAD,

	/**
	 * Execute a test code in different thread from the JUnit execution.
	 */
	SEPARATE_THREAD
}
