/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.executor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;

// TODO This class should become some kind of "JUnit" component, that will be initialized during start up
public class TestExecutorRegistryImpl implements TestExecutorRegistry {
	private List<TestExecutor> testExecutors = new LinkedList<>();

	@Override
	public void executeAll(ExecutionRequest request, TestDescriptor testDescriptor) {
		testExecutors.stream().filter(testExecutor -> testExecutor.canExecute(testDescriptor)).forEach(
			testExecutor -> testExecutor.execute(request, testDescriptor));
	}

	@Override
	public void register(TestExecutor testExecutor) {
		testExecutors.add(testExecutor);
		testExecutor.setTestExecutorRegistry(this);
	}
}
