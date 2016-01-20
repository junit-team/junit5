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

/**
 * Main entry point for client code that wants to <em>discover</em>
 * and <em>execute</em> tests using dynamically registered test engines.
 *
 * <p>You get hold of an instance of this interface by using
 * <pre>
 *     JUnit5Launcher.get()
 * </pre>
 *
 * <p>Test engines are registered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} facility. For that purpose, a
 * text file named {@code META-INF/services/org.junit.gen5.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.gen5.engine.TestEngine} interface is stated.
 *
 * <p>Discovering or executing tests requires a {@link TestDiscoveryRequest}
 * which is passed to all registered engines. Each engine decides which tests
 * it can discover and later execute according to this {@link TestDiscoveryRequest}.
 *
 * <p>Users of this class may optionally call {@link #discover} prior to
 * {@link #execute} in order to inspect the {@link TestPlan} before executing
 * it.
 *
 * <p>Prior to executing tests, users of this class should
 * {@linkplain #registerTestExecutionListeners register} one or more
 * {@link TestExecutionListener} instances in order to get feedback about the
 * progress and results of test execution. Listeners are notified of events
 * in the order in which they were registered.
 *
 * @since 5.0
 * @see TestDiscoveryRequest
 * @see TestPlan
 * @see TestExecutionListener
 * @see org.junit.gen5.launcher.main.JUnit5Launcher
 */
public interface Launcher {

	/**
	 * Register one or more listeners for test execution.
	 *
	 * @param listeners the listeners to be notified of test execution events
	 */
	void registerTestExecutionListeners(TestExecutionListener... listeners);

	/**
	 * Discover tests and build a {@link TestPlan} according to the supplied
	 * {@link TestDiscoveryRequest} by querying all registered engines and
	 * collecting their results.
	 *
	 * @param discoveryRequest the discovery request
	 * @return a {@code TestPlan} that contains all resolved
	 *         {@linkplain TestIdentifier identifiers} from all registered engines
	 */
	TestPlan discover(TestDiscoveryRequest discoveryRequest);

	/**
	 * Execute a {@link TestPlan} which is built according to the supplied
	 * {@link TestDiscoveryRequest} by querying all registered engines and
	 * collecting their results, and notify {@linkplain #registerTestExecutionListeners
	 * registered listeners} about the progress and results of the execution.
	 *
	 * @param discoveryRequest the discovery request to be executed
	 */
	void execute(TestDiscoveryRequest discoveryRequest);

}
