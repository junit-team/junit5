/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Implementations of this interface are responsible for determining
 * configuration used for creating {@link Launcher} instances via {@link LauncherFactory}.
 *
 * <h4>Example</h4>
 *
 * <pre class="code">
 * import static org.junit.platform.launcher.core.*;
 * // ...
 *
 *   LauncherConfig.builder()
 *     .setTestEngineAutoRegistrationEnabled(true)
 *     .setTestExecutionListenerAutoRegistrationEnabled(true)
 *     .addAdditionalTestEngines(new CustomTestEngine())
 *     .addAdditionalTestExecutionListeners(new CustomTestExecutionListener())
 *     .build();
 * </pre>
 *
 * @see Launcher
 * @see LauncherFactory
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public interface LauncherConfig {

	/**
	 * Returns true if test engines should be discovered at runtime using the
	 * {@link java.util.ServiceLoader ServiceLoader} facility and false otherwise.
	 *
	 * @return true if test engines should be discovered at runtime and false otherwise
	 */
	boolean isTestEngineAutoRegistrationEnabled();

	/**
	 * Returns additional test engines that should be added to created
	 * {@link Launcher} instance.
	 *
	 * @return additional test engines
	 */
	Collection<TestEngine> getAdditionalTestEngines();

	/**
	 * Returns true if test execution listeners should be discovered at runtime using the
	 * {@link java.util.ServiceLoader ServiceLoader} facility and false otherwise.
	 *
	 * @return true if test execution listeners should be discovered at runtime and false otherwise
	 */
	boolean isTestExecutionListenerAutoRegistrationEnabled();

	/**
	 * Returns additional test execution listeners that should be added to created
	 * {@link Launcher} instance.
	 *
	 * @return additional test execution listeners
	 */
	Collection<TestExecutionListener> getAdditionalTestExecutionListeners();

	/**
	 * Create a new {@link LauncherConfig.Builder}.
	 *
	 * @return a new builder
	 */
	static Builder builder() {
		return new Builder();
	}

	class Builder {

		private boolean testExecutionListenerAutoRegistrationEnabled = true;

		private boolean testEngineAutoRegistrationEnabled = true;

		private final Collection<TestEngine> additionalTestEngines = new ArrayList<>();

		private final Collection<TestExecutionListener> additionalTestExecutionListeners = new ArrayList<>();

		/**
		 * Configure test execution listeners auto detection.
		 *
		 * @param enabled true if test execution listeners should be auto detected and false otherwise.
		 * @return this builder for method chaining
		 */
		public Builder setTestExecutionListenerAutoRegistrationEnabled(boolean enabled) {
			this.testExecutionListenerAutoRegistrationEnabled = enabled;
			return this;
		}

		/**
		 * Configure test engines auto detection.
		 *
		 * @param enabled true if test engines should be auto detected and false otherwise.
		 * @return this builder for method chaining
		 */
		public Builder setTestEngineAutoRegistrationEnabled(boolean enabled) {
			this.testEngineAutoRegistrationEnabled = enabled;
			return this;
		}

		/**
		 * Add all of the supplied {@code engines} to the configuration.
		 *
		 * @param engines the list of additional test engines to add;
		 * never {@code null}
		 * @return this builder for method chaining
		 */
		public Builder addAdditionalTestEngines(TestEngine... engines) {
			Collections.addAll(this.additionalTestEngines, engines);
			return this;
		}

		/**
		 * Add all of the supplied {@code listeners} to the configuration.
		 *
		 * @param listeners the list of additional test execution listeners to add;
		 * never {@code null}
		 * @return this builder for method chaining
		 */
		public Builder addAdditionalTestExecutionListeners(TestExecutionListener... listeners) {
			Collections.addAll(this.additionalTestExecutionListeners, listeners);
			return this;
		}

		/**
		 * Build the {@link LauncherConfig} that has been configured via
		 * this builder.
		 */
		public LauncherConfig build() {
			return new DefaultLauncherConfig(testEngineAutoRegistrationEnabled, additionalTestEngines,
				testExecutionListenerAutoRegistrationEnabled, additionalTestExecutionListeners);
		}

	}
}
