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
import org.junit.gen5.engine.junit5ext.TestBody;
import org.junit.gen5.engine.junit5ext.TestHandler;
import org.junit.gen5.engine.junit5ext.executor.ExecutionContext;

public class TestHandlerSpy implements TestHandler {
	public ExecutionContext foundExecutionContext;
	public TestDescriptor foundTestDescriptor;

	@Override
	public void invoke(ExecutionContext context, TestBody runnable) {
		foundExecutionContext = context;
		foundTestDescriptor = context.getTestDescriptor();
	}
}