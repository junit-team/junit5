/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class JupiterEngineExecutionContext implements EngineExecutionContext {

	private static final Logger logger = LoggerFactory.getLogger(JupiterEngineExecutionContext.class);

	private final State state;

	// The following is not "cloneable" State.
	private boolean beforeAllCallbacksExecuted = false;
	private boolean beforeAllMethodsExecuted = false;

	public JupiterEngineExecutionContext(EngineExecutionListener executionListener,
			ConfigurationParameters configurationParameters) {
		this(new State(executionListener, configurationParameters));
	}

	private JupiterEngineExecutionContext(State state) {
		this.state = state;
	}

	public void close() throws Exception {
		ExtensionContext extensionContext = getExtensionContext();
		if (extensionContext instanceof AutoCloseable) {
			try {
				((AutoCloseable) extensionContext).close();
			}
			catch (Exception e) {
				logger.error(e, () -> "Caught exception while closing extension context: " + extensionContext);
				throw e;
			}
		}
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

	public ThrowableCollector getThrowableCollector() {
		return this.state.throwableCollector;
	}

	/**
	 * Track that an attempt was made to execute {@code BeforeAllCallback} extensions.
	 *
	 * @since 5.3
	 */
	public void beforeAllCallbacksExecuted(boolean beforeAllCallbacksExecuted) {
		this.beforeAllCallbacksExecuted = beforeAllCallbacksExecuted;
	}

	/**
	 * @return {@code true} if an attempt was made to execute {@code BeforeAllCallback}
	 * extensions
	 * @since 5.3
	 */
	public boolean beforeAllCallbacksExecuted() {
		return beforeAllCallbacksExecuted;
	}

	/**
	 * Track that an attempt was made to execute {@code @BeforeAll} methods.
	 */
	public void beforeAllMethodsExecuted(boolean beforeAllMethodsExecuted) {
		this.beforeAllMethodsExecuted = beforeAllMethodsExecuted;
	}

	/**
	 * @return {@code true} if an attempt was made to execute {@code @BeforeAll}
	 * methods
	 */
	public boolean beforeAllMethodsExecuted() {
		return this.beforeAllMethodsExecuted;
	}

	public Builder extend() {
		return new Builder(this.state);
	}

	private static final class State implements Cloneable {

		final EngineExecutionListener executionListener;
		final ConfigurationParameters configurationParameters;
		TestInstanceProvider testInstanceProvider;
		ExtensionRegistry extensionRegistry;
		ExtensionContext extensionContext;
		ThrowableCollector throwableCollector;

		State(EngineExecutionListener executionListener, ConfigurationParameters configurationParameters) {
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
		private State newState = null;

		private Builder(State originalState) {
			this.originalState = originalState;
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

		public Builder withThrowableCollector(ThrowableCollector throwableCollector) {
			newState().throwableCollector = throwableCollector;
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
