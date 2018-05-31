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

import java.util.List;
import java.util.concurrent.Future;

import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.engine.TestDescriptor;

public interface HierarchicalTestExecutorService extends AutoCloseable {

	Future<Void> submit(TestTask testTask);

	void invokeAll(List<? extends TestTask> testTasks);

	/**
	 * Overridden to avoid warning caused by a javac bug:
	 * https://bugs.openjdk.java.net/browse/JDK-8155591
	 */
	@Override
	void close();

	interface TestTask {

		TestDescriptor getTestDescriptor();

		ExecutionMode getExecutionMode();

		ResourceLock getResourceLock();

		void execute();

	}
}
