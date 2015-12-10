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

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

public class ExecutionContext {
	private TestExecutionListener testExecutionListener;

	private TestDescriptor testDescriptor;

	public TestExecutionListener getTestExecutionListener() {
		return testExecutionListener;
	}

	public void setTestExecutionListener(TestExecutionListener testExecutionListener) {
		this.testExecutionListener = testExecutionListener;
	}

	public <T extends TestDescriptor> T getTestDescriptor() {
		return (T) testDescriptor;
	}

	public void setTestDescriptor(TestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	public static Builder contextForDescriptor(TestDescriptor testDescriptor) {
		return new Builder().withTestDescriptor(testDescriptor);
	}

	public static Builder cloneContext(ExecutionContext context) {
		return new Builder().withTestExecutionListener(context.getTestExecutionListener()).withTestDescriptor(
			context.getTestDescriptor());
	}

	public static class Builder {
		ExecutionContext context = new ExecutionContext();

		public Builder withTestDescriptor(TestDescriptor testDescriptor) {
			context.setTestDescriptor(testDescriptor);
			return this;
		}

		public Builder withTestExecutionListener(TestExecutionListener testExecutionListener) {
			context.setTestExecutionListener(testExecutionListener);
			return this;
		}

		public ExecutionContext build() {
			return context;
		}
	}
}
