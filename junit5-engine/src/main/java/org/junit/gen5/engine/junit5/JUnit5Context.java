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

import org.junit.gen5.engine.Context;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

public class JUnit5Context implements Context {

	private final State state;

	public JUnit5Context() {
		this(new State());
	}

	private JUnit5Context(State state) {
		this.state = state;
	}

	public TestInstanceProvider getTestInstanceProvider() {
		return state.testInstanceProvider;
	}

	public BeforeEachCallback getBeforeEachCallback() {
		return state.beforeEachCallback;
	}

	public TestExtensionRegistry getTestExtensionRegistry() {
		return state.testExtensionRegistry;
	}

	public Builder extend() {
		return builder(this);
	}

	public static Builder builder() {
		return new Builder(null, new State());
	}

	public static Builder builder(JUnit5Context context) {
		return new Builder(context.state, null);
	}

	private static final class State implements Cloneable {

		TestInstanceProvider testInstanceProvider;
		BeforeEachCallback beforeEachCallback;
		TestExtensionRegistry testExtensionRegistry;

		@Override
		public State clone() {
			try {
				return (State) super.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new RuntimeException("State could not be cloned", e);
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

		public Builder withBeforeEachCallback(BeforeEachCallback beforeEachCallback) {
			newState().beforeEachCallback = beforeEachCallback;
			return this;
		}

		public Builder withTestExtensionRegistry(TestExtensionRegistry testExtensionRegistry) {
			newState().testExtensionRegistry = testExtensionRegistry;
			return this;
		}

		public JUnit5Context build() {
			if (newState != null) {
				originalState = newState;
				newState = null;
			}
			return new JUnit5Context(originalState);
		}

		private State newState() {
			if (newState == null) {
				this.newState = originalState.clone();
			}
			return newState;
		}

	}

}
