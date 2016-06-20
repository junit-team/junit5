/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

/**
 * @since 5.0
 */
@API(Internal)
public class JupiterEngineExecutionContext implements EngineExecutionContext {

	private final State state;

	public JupiterEngineExecutionContext(EngineExecutionListener executionListener,
			ConfigurationParameters configurationParameters) {
		this(new State(executionListener, configurationParameters));
	}

	private JupiterEngineExecutionContext(State state) {
		this.state = state;
	}

	public EngineExecutionListener getExecutionListener() {
		return this.state.executionListener;
	}

	public ConfigurationParameters getConfigurationParameters() {
		return this.state.configurationParameters;
	}

	public TestInstanceProvider getTestInstanceProvider() {
		return this.state.testInstanceProvider;
	}

	public ExtensionRegistry getExtensionRegistry() {
		return this.state.extensionRegistry;
	}

	public ExtensionContext getExtensionContext() {
		return this.state.extensionContext;
	}

	public Builder extend() {
		return builder(this);
	}

	public static Builder builder(JupiterEngineExecutionContext context) {
		return new Builder(context.state, null);
	}

	private static final class State implements Cloneable {

		final EngineExecutionListener executionListener;
		final ConfigurationParameters configurationParameters;
		TestInstanceProvider testInstanceProvider;
		ExtensionRegistry extensionRegistry;
		ExtensionContext extensionContext;

		public State(EngineExecutionListener executionListener, ConfigurationParameters configurationParameters) {
			this.executionListener = executionListener;
			this.configurationParameters = configurationParameters;
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

		public Builder withExtensionRegistry(ExtensionRegistry extensionRegistry) {
			newState().extensionRegistry = extensionRegistry;
			return this;
		}

		public Builder withExtensionContext(ExtensionContext extensionContext) {
			newState().extensionContext = extensionContext;
			return this;
		}

		public JupiterEngineExecutionContext build() {
			if (newState != null) {
				originalState = newState;
				newState = null;
			}
			return new JupiterEngineExecutionContext(originalState);
		}

		private State newState() {
			if (newState == null) {
				this.newState = originalState.clone();
			}
			return newState;
		}

	}

}
