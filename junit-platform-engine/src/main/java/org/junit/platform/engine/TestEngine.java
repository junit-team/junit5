/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;

/**
 * A {@code TestEngine} facilitates <em>discovery</em> and <em>execution</em> of
 * tests for a particular programming model.
 *
 * <p>For example, JUnit provides a {@code TestEngine} that discovers and
 * executes tests written using the JUnit Jupiter programming model.
 *
 * <p>Every {@code TestEngine} must {@linkplain #getId provide an ID},
 * {@linkplain #discover discover tests} from
 * {@link EngineDiscoveryRequest EngineDiscoveryRequests},
 * and {@linkplain #execute execute them} according to
 * {@link ExecutionRequest ExecutionRequests}.
 *
 * @see EngineDiscoveryRequest
 * @see ExecutionRequest
 * @since 1.0
 */
@API(Experimental)
public interface TestEngine {

	/**
	 * Get the ID that uniquely identifies this test engine.
	 *
	 * <p>Each test engine must provide a unique ID. JUnit Vintage and Jupiter
	 * use {@code "junit-vintage"} and {@code "junit-jupiter"}, respectively.
	 * When in doubt, you may use the fully qualified name of your custom
	 * {@code TestEngine} implementation class.
	 */
	String getId();

	/**
	 * Discover tests according to an {@link EngineDiscoveryRequest}.
	 *
	 * <p>The supplied {@code uniqueId} must be used for the returned
	 * {@link TestDescriptor}. In addition, it is used to create unique IDs for
	 * its children by calling {@link UniqueId#append}.
	 *
	 * @param discoveryRequest the request to discover tests from
	 * @param uniqueId the unique ID to be used for this test engine's
	 * {@code TestDescriptor}
	 * @return the root {@code TestDescriptor} of this engine
	 */
	TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId);

	/**
	 * Execute tests according to an {@link ExecutionRequest}.
	 *
	 * <p>The {@code request} passed to this method contains the root
	 * {@link TestDescriptor} that was previously returned by {@link #discover},
	 * the {@link EngineExecutionListener} to be notified of test execution
	 * events, and {@link ConfigurationParameters} that may influence test execution.
	 *
	 * @param request the request to execute tests for
	 */
	void execute(ExecutionRequest request);

}
