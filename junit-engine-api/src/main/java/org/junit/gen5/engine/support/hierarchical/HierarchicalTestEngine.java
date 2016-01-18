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
 * Abstract superclass of all TestEngine implementations that are willing to use the {@linkplain Container} and
 * {@linkplain Leaf} nodes as main principle to organize test suites.
 *
 * @param <C> The concrete type of {@linkplain EngineExecutionContext} used by a concrete subclass.
 */
public abstract class HierarchicalTestEngine<C extends EngineExecutionContext> implements TestEngine {

	@Override
	public final void execute(ExecutionRequest request) {
		new HierarchicalTestExecutor<>(request, createExecutionContext(request)).execute();
	}

	protected abstract C createExecutionContext(ExecutionRequest request);

}
