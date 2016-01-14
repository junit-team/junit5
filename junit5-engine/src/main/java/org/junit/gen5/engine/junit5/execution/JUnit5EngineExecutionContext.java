/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.*;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.EngineExecutionContext;

/**
 * @since 5.0
 */
public class JUnit5EngineExecutionContext implements EngineExecutionContext {

	private final State state;

	public JUnit5EngineExecutionContext(EngineExecutionListener executionListener) {
		this(new State(executionListener));
	}

	private JUnit5EngineExecutionContext(State state) {
		this.state = state;
	}

	public EngineExecutionListener getExecutionListener() {
		return this.state.executionListener;
	}

	public TestInstanceProvider getTestInstanceProvider() {
		return this.state.testInstanceProvider;
	}

	public TestExtensionRegistry getTestExtensionRegistry() {
		return this.state.testExtensionRegistry;
	}

	public ExtensionContext getExtensionContext() {
		return this.state.extensionContext;
	}

	public Builder extend() {
		return builder(this);
	}

	public static Builder builder(JUnit5EngineExecutionContext context) {
		return new Builder(context.state, null);
	}

	private static final class State implements Cloneable {

		final EngineExecutionListener executionListener;
		TestInstanceProvider testInstanceProvider;
		TestExtensionRegistry testExtensionRegistry;
		ExtensionContext extensionContext;

		public State(EngineExecutionListener executionListener) {
			this.executionListener = executionListener;
		}

		@Override
		public State clone() {
			try {
				return (State) super.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new JUnitException("State could not be cloned", e);
			}
		}

	}

	public static class Builder {

		private State originalState;
		private State newState;

		private Builder(State originalState, State state) {
			this.originalState = originalState;
			this.newState = state;
		}

		public Builder withTestInstanceProvider(TestInstanceProvider testInstanceProvider) {
			newState().testInstanceProvider = testInstanceProvider;
			return this;
		}

		public Builder withTestExtensionRegistry(TestExtensionRegistry testExtensionRegistry) {
			newState().testExtensionRegistry = testExtensionRegistry;
			return this;
		}

		public Builder withExtensionContext(ExtensionContext extensionContext) {
			newState().extensionContext = extensionContext;
			return this;
		}

		public JUnit5EngineExecutionContext build() {
			if (newState != null) {
				originalState = newState;
				newState = null;
			}
			return new JUnit5EngineExecutionContext(originalState);
		}

		private State newState() {
			if (newState == null) {
				this.newState = originalState.clone();
			}
			return newState;
		}

	}

}
