/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * The {@code Launcher} API is the main entry point for client code that
 * wishes to <em>discover</em> and <em>execute</em> tests using one or more
 * {@linkplain org.junit.gen5.engine.TestEngine test engines}.
 *
 * <p>Implementations of this interface are responsible for determining
 * the set of test engines to delegate to at runtime and for ensuring that
 * each test engine has an {@linkplain org.junit.gen5.engine.TestEngine#getId ID}
 * that is unique among the registered test engines. For example, the
 * default implementation returned by
 * {@link org.junit.gen5.launcher.core.LauncherFactory#create LauncherFactory.create()}
 * dynamically discovers test engines via Java's
 * {@link java.util.ServiceLoader ServiceLoader} mechanism.
 *
 * <p>Discovery and execution of tests require a {@link TestDiscoveryRequest}
 * which is passed to all registered engines. Each engine decides which tests
 * it can discover and later execute according to the {@link TestDiscoveryRequest}.
 *
 * <p>Clients of this interface may optionally call {@link #discover} prior to
 * {@link #execute} in order to inspect the {@link TestPlan} before executing
 * it.
 *
 * <p>Prior to executing tests, clients of this interface should
 * {@linkplain #registerTestExecutionListeners register} one or more
 * {@link TestExecutionListener} instances in order to get feedback about the
 * progress and results of test execution. Listeners will be notified of events
 * in the order in which they were registered.
 *
 * @since 5.0
 * @see TestDiscoveryRequest
 * @see TestPlan
 * @see TestExecutionListener
 * @see org.junit.gen5.launcher.core.LauncherFactory
 * @see org.junit.gen5.engine.TestEngine
 */
@API(Experimental)
public interface Launcher {

	/**
	 * Register one or more listeners for test execution.
	 *
	 * @param listeners the listeners to be notified of test execution events;
	 * never {@code null}
	 */
	void registerTestExecutionListeners(TestExecutionListener... listeners);

	/**
	 * Discover tests and build a {@link TestPlan} according to the supplied
	 * {@link TestDiscoveryRequest} by querying all registered engines and
	 * collecting their results.
	 *
	 * @param testDiscoveryRequest the test discovery request; never {@code null}
	 * @return a {@code TestPlan} that contains all resolved {@linkplain
	 * TestIdentifier identifiers} from all registered engines
	 */
	TestPlan discover(TestDiscoveryRequest testDiscoveryRequest);

	/**
	 * Execute a {@link TestPlan} which is built according to the supplied
	 * {@link TestDiscoveryRequest} by querying all registered engines and
	 * collecting their results, and notify {@linkplain #registerTestExecutionListeners
	 * registered listeners} about the progress and results of the execution.
	 *
	 * @param testDiscoveryRequest the test discovery request; never {@code null}
	 */
	void execute(TestDiscoveryRequest testDiscoveryRequest);

}
