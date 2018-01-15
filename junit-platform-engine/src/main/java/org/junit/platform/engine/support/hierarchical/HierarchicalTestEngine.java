/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.MAINTAINED;

import org.apiguardian.api.API;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestEngine;

/**
 * Abstract base class for all {@link TestEngine} implementations that wish
 * to organize test suites hierarchically based on the {@link Node} abstraction.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by this engine
 * @since 1.0
 * @see Node
 */
@API(status = MAINTAINED, since = "1.0")
public abstract class HierarchicalTestEngine<C extends EngineExecutionContext> implements TestEngine {

	/**
	 * Create an initial {@linkplain #createExecutionContext execution
	 * context}, execute the behavior of all {@linkplain Node nodes} in the
	 * hierarchy starting with the supplied {@code request}'s
	 * {@linkplain ExecutionRequest#getRootTestDescriptor() root} and notify
	 * its {@linkplain ExecutionRequest#getEngineExecutionListener() execution
	 * listener} of test execution events.
	 *
	 * @see Node
	 * @see #createExecutionContext
	 */
	@Override
	public final void execute(ExecutionRequest request) {
		new HierarchicalTestExecutor<>(request, createExecutionContext(request)).execute();
	}

	/**
	 * Create the initial execution context for executing the supplied
	 * {@link ExecutionRequest request}.
	 *
	 * @param request the request about to be executed
	 * @return the initial context that will be passed to nodes in the hierarchy
	 */
	protected abstract C createExecutionContext(ExecutionRequest request);

}
