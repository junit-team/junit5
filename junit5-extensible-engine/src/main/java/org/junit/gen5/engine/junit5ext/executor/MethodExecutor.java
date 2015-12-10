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

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5ext.descriptor.MethodDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class MethodExecutor implements TestExecutor {
	private TestExecutorRegistry testExecutorRegistry;

	@Override
	public void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry) {
		this.testExecutorRegistry = testExecutorRegistry;
	}

	@Override
	public boolean canExecute(ExecutionContext context) {
		return context.getTestDescriptor() instanceof MethodDescriptor;
	}

	@Override
	public void execute(ExecutionContext context) throws TestSkippedException, TestAbortedException, AssertionError {
		MethodDescriptor testDescriptor = context.getTestDescriptor();
		context.getTestHandler().invoke(context, () -> {
			ReflectionUtils.invokeMethod(testDescriptor.getMethod(), context.getTestInstance());
		});
	}
}
