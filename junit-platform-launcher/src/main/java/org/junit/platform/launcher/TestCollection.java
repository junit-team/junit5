/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * A collection of tests that can be examined, filtered and executed.
 *
 * @since 1.1
 */
@API(status = STABLE, since = "1.1")
public interface TestCollection {

	/**
	 * Get the {@link TestPlan} created from processing the
	 * {@link LauncherDiscoveryRequest}.
	 */
	TestPlan testPlan();

	/**
	 * Apply the given filters to the discovered tests.
	 */
	void applyPostDiscoveryFilters(PostDiscoveryFilter... filters);

	/**
	 * Execute the tests and notify {@linkplain #registerTestExecutionListeners
	 * registered listeners} about the progress and results of the execution.
	 *
	 * <p>Supplied test execution listeners are registered in addition to listeners
	 * that were registered at the time this {@code DiscoveredTests} instance was
	 * created.
	 *
	 * @param listeners additional test execution listeners; never {@code null}
	 */
	void execute(TestExecutionListener... listeners);
}
