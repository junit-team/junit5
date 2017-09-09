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
 * A {@link Launcher} implementation that allows the caller to collect tests
 * and filter them before executing the tests.
 *
 * @since 1.1
 * @see LauncherDiscoveryRequest
 * @see TestPlan
 * @see TestExecutionListener
 * @see org.junit.platform.launcher.core.LauncherFactory
 * @see org.junit.platform.engine.TestEngine
 */
@API(status = STABLE, since = "1.1")
public interface CollectingLauncher extends Launcher {

	/**
	 * Collect tests according to the supplied {@link LauncherDiscoveryRequest}
	 * by querying all registered engines and collecting their results.
	 *
	 * <p>Any {@link TestExecutionListener} instances registered before this
	 * call will be notified if {@link TestCollection#execute(TestExecutionListener...)}
	 * is called.
	 *
	 * @param launcherDiscoveryRequest the launcher discovery request; never {@code null}
	 * @return a {@code Request} instance built from all resolved {@linkplain
	 * TestIdentifier identifiers} from all registered engines
	 */
	TestCollection collect(LauncherDiscoveryRequest launcherDiscoveryRequest);

	default TestPlan discover(LauncherDiscoveryRequest launcherDiscoveryRequest) {
		return collect(launcherDiscoveryRequest).testPlan();
	}
}
