/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.Future;

public class SameThreadHierarchicalTestExecutorService<C extends EngineExecutionContext>
		implements HierarchicalTestExecutorService<C> {

	@Override
	public Future<Void> submit(TestTask<C> testTask) {
		testTask.execute();
		return completedFuture(null);
	}

	@Override
	public void close() {
		// nothing to do
	}

}
