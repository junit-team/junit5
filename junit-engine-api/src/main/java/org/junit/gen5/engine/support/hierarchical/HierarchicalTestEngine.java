/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestEngine;

/**
 * Abstract base class for all {@link TestEngine} implementations that wish
 * to organize test suites hierarchically based on the {@link Node},
 * {@link Container}, and {@link Leaf} abstractions.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by this engine
 * @since 5.0
 * @see Node
 * @see Container
 * @see Leaf
 */
public abstract class HierarchicalTestEngine<C extends EngineExecutionContext> implements TestEngine {

	@Override
	public final void execute(ExecutionRequest request) {
		new HierarchicalTestExecutor<>(request, createExecutionContext(request)).execute();
	}

	protected abstract C createExecutionContext(ExecutionRequest request);

}
