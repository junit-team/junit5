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

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.List;
import java.util.concurrent.Future;

public class SameThreadHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	@Override
	public Future<Void> submit(TestTask testTask) {
		testTask.execute();
		return completedFuture(null);
	}

	@Override
	public void invokeAll(List<? extends TestTask> tasks) {
		tasks.forEach(TestTask::execute);
	}

	@Override
	public void close() {
		// nothing to do
	}

}
