/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.ExecutionContext;
import org.junit.gen5.engine.junit5ext.executor.TestExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistry;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class AlwaysMatchingTestExecutorSpy implements TestExecutor {
	public TestExecutorRegistry foundTestExecutorRegistry;
	public TestDescriptor foundTestDescriptor;
	public ExecutionContext foundExecutionContext;
	public TestDescriptor foundTestDescriptorForExecution;

	@Override
	public void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry) {
		foundTestExecutorRegistry = testExecutorRegistry;
	}

	@Override
	public boolean canExecute(ExecutionContext context) {
		foundTestDescriptor = context.getTestDescriptor();
		return true;
	}

	@Override
	public void execute(ExecutionContext context) throws TestSkippedException, TestAbortedException, AssertionError {
		foundExecutionContext = context;
		foundTestDescriptorForExecution = context.getTestDescriptor();
	}
}
