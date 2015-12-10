/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext;

import java.lang.reflect.InvocationTargetException;

import org.junit.gen5.engine.junit5ext.executor.ExecutionContext;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class TestHandlerImpl implements TestHandler {
	@Override
	public void invoke(ExecutionContext context, TestBody runnable) {
		try {
			context.getTestExecutionListener().testStarted(context.getTestDescriptor());
			try {
				// TODO Add Extension Point for test execution
				runnable.runTest();
			}
			catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
			context.getTestExecutionListener().testSucceeded(context.getTestDescriptor());
		}
		catch (TestAbortedException e) {
			context.getTestExecutionListener().testAborted(context.getTestDescriptor(), e);
		}
		catch (TestSkippedException e) {
			context.getTestExecutionListener().testSkipped(context.getTestDescriptor(), e);
		}
		catch (Throwable e) {
			context.getTestExecutionListener().testFailed(context.getTestDescriptor(), e);
		}
	}
}
