/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import org.junit.gen5.engine.*;
import org.junit.gen5.engine.junit5.task.*;
import org.opentestalliance.*;
import java.lang.reflect.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;


public class RootTask implements ExecutionTask {


	private TestExecutionListener testExecutionListener;
	private JavaMethodTestDescriptor testDescriptor;


	public RootTask(TestExecutionListener testExecutionListener, JavaMethodTestDescriptor testDescriptor) {

		this.testExecutionListener = testExecutionListener;
		this.testDescriptor = testDescriptor;

	}


	@Override
	public void execute() throws Exception {
		this.handleSingleDescriptor(this.testExecutionListener, this.testDescriptor);
	}




	private void handleSingleDescriptor(TestExecutionListener testExecutionListener, JavaMethodTestDescriptor testDescriptor) {

		try {
			testExecutionListener.testStarted(testDescriptor);


			Object instance = newInstance(testDescriptor.getTestClass());
			JavaTestMethodTask task = new JavaTestMethodTask(testDescriptor.getTestClass(), testDescriptor.getTestMethod(), instance);
			task.execute();

			testExecutionListener.testSucceeded(testDescriptor);
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof TestSkippedException) {
				testExecutionListener.testSkipped(testDescriptor, targetException);
			}
			else if (targetException instanceof TestAbortedException) {
				testExecutionListener.testAborted(testDescriptor, targetException);
			}
			else {
				testExecutionListener.testFailed(testDescriptor, targetException);
			}
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
			throw new IllegalStateException(String.format("Test %s is not well-formed and cannot be executed",
					testDescriptor.getUniqueId()), ex);
		}
		catch (Exception ex) {
			testExecutionListener.testFailed(testDescriptor, ex);
		}
	}

}
