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
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.executor.ExecutionContext;

public class ExecutionContextStubForTestDescriptor extends ExecutionContext {
	private final TestDescriptor testDescriptor;

	public ExecutionContextStubForTestDescriptor(TestDescriptor testDescriptor) {

		this.testDescriptor = testDescriptor;
	}

	@Override
	public TestExecutionListener getTestExecutionListener() {
		return null;
	}

	@Override
	public void setTestExecutionListener(TestExecutionListener testExecutionListener) {
	}

	@Override
	public <T extends TestDescriptor> T getTestDescriptor() {
		return (T) testDescriptor;
	}

	@Override
	public void setTestDescriptor(TestDescriptor testDescriptor) {
	}
}