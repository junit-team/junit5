/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

/**
 * Represents a strategy for scheduling when individual test methods
 * should be run (in serial or parallel)
 *
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public interface RunnerScheduler {
	/**
	 * Schedule a child statement to run
	 */
	void schedule(Runnable childStatement);

	/**
	 * Override to implement any behavior that must occur
	 * after all children have been scheduled (for example,
	 * waiting for them all to finish)
	 */
	void finished();
}
