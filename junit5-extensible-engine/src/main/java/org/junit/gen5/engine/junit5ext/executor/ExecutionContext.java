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
import org.junit.gen5.engine.junit5ext.TestHandler;
import org.junit.gen5.engine.junit5ext.TestHandlerImpl;

public class ExecutionContext implements Cloneable {
	public static Builder contextForDescriptor(TestDescriptor testDescriptor) {
		return new Builder().withTestDescriptor(testDescriptor);
	}

	public static Builder cloneContext(ExecutionContext context) {
		return new Builder(context);
	}

	private TestHandler testHandler;

	private TestExecutionListener testExecutionListener;

	private TestDescriptor testDescriptor;

	private Object testInstance;

	@Override
	protected Object clone() throws CloneNotSupportedException {
		ExecutionContext clone = new ExecutionContext();
		clone.testExecutionListener = this.testExecutionListener;
		clone.testDescriptor = this.testDescriptor;
		clone.testInstance = this.testInstance;
		clone.testHandler = this.testHandler;
		return clone;
	}

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

	public Object getTestInstance() {
		return testInstance;
	}

	public void setTestInstance(Object testInstance) {
		this.testInstance = testInstance;
	}

	public TestHandler getTestHandler() {
		return testHandler;
	}

	public void setTestHandler(TestHandler testHandler) {
		this.testHandler = testHandler;
	}

	public static class Builder {
		private ExecutionContext context;

		private Builder() {
			this.context = new ExecutionContext();
			context.setTestHandler(new TestHandlerImpl());
		}

		private Builder(ExecutionContext context) {
			try {
				this.context = (ExecutionContext) context.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new IllegalStateException("Cannot clone execution context!", e);
			}
		}

		public Builder withTestDescriptor(TestDescriptor testDescriptor) {
			context.setTestDescriptor(testDescriptor);
			return this;
		}

		public Builder withTestExecutionListener(TestExecutionListener testExecutionListener) {
			context.setTestExecutionListener(testExecutionListener);
			return this;
		}

		public ExecutionContext build() {
			context.setTestHandler(new TestHandlerImpl());
			return context;
		}
	}
}
