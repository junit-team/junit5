/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * {@code LauncherConfig} defines the configuration API for creating
 * {@link Launcher} instances via the {@link LauncherFactory}.
 *
 * <h4>Example</h4>
 *
 * <pre class="code">
 * LauncherConfig launcherConfig = LauncherConfig.builder()
 *   .enableTestEngineAutoRegistration(false)
 *   .enableTestExecutionListenerAutoRegistration(false)
 *   .addTestEngines(new CustomTestEngine())
 *   .addTestExecutionListeners(new CustomTestExecutionListener())
 *   .build();
 *
 * Launcher launcher = LauncherFactory.create(launcherConfig);
 * LauncherDiscoveryRequest discoveryRequest = ...
 * launcher.execute(discoveryRequest);
 * </pre>
 *
 * @see #builder()
 * @see Launcher
 * @see LauncherFactory
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public interface LauncherConfig {

	/**
	 * Determine if test engines should be discovered at runtime using the
	 * {@link java.util.ServiceLoader ServiceLoader} mechanism and
	 * automatically registered.
	 *
	 * @return {@code true} if test engines should be automatically registered
	 */
	boolean isTestEngineAutoRegistrationEnabled();

	/**
	 * Determine if test execution listeners should be discovered at runtime
	 * using the {@link java.util.ServiceLoader ServiceLoader} mechanism and
	 * automatically registered.
	 *
	 * @return {@code true} if test execution listeners should be automatically
	 * registered
	 */
	boolean isTestExecutionListenerAutoRegistrationEnabled();

	/**
	 * Get the collection of additional test engines that should be added to
	 * the {@link Launcher}.
	 *
	 * @return the collection of additional test engines; never {@code null} but
	 * potentially empty
	 */
	Collection<TestEngine> getAdditionalTestEngines();

	/**
	 * Get the collection of additional test execution listeners that should be
	 * added to the {@link Launcher}.
	 *
	 * @return the collection of additional test execution listeners; never
	 * {@code null} but potentially empty
	 */
	Collection<TestExecutionListener> getAdditionalTestExecutionListeners();

	/**
	 * Create a new {@link LauncherConfig.Builder}.
	 *
	 * @return a new builder; never {@code null}
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * <em>Builder</em> API for {@link LauncherConfig}.
	 */
	class Builder {

		private boolean listenerAutoRegistrationEnabled = true;

		private boolean engineAutoRegistrationEnabled = true;

		private final Collection<TestEngine> engines = new LinkedHashSet<>();

		private final Collection<TestExecutionListener> listeners = new LinkedHashSet<>();

		private Builder() {
			/* no-op */
		}

		/**
		 * Configure the auto-registration flag for test execution listeners.
		 *
		 * <p>Defaults to {@code true}.
		 *
		 * @param enabled {@code true} if test execution listeners should be
		 * automatically registered
		 * @return this builder for method chaining
		 */
		public Builder enableTestExecutionListenerAutoRegistration(boolean enabled) {
			this.listenerAutoRegistrationEnabled = enabled;
			return this;
		}

		/**
		 * Configure the auto-registration flag for test engines.
		 *
		 * <p>Defaults to {@code true}.
		 *
		 * @param enabled {@code true} if test engines should be automatically
		 * registered
		 * @return this builder for method chaining
		 */
		public Builder enableTestEngineAutoRegistration(boolean enabled) {
			this.engineAutoRegistrationEnabled = enabled;
			return this;
		}

		/**
		 * Add all of the supplied {@code engines} to the configuration.
		 *
		 * @param engines additional test engines to register; never {@code null}
		 * or containing {@code null}
		 * @return this builder for method chaining
		 */
		public Builder addTestEngines(TestEngine... engines) {
			Preconditions.notNull(engines, "TestEngine array must not be null");
			Preconditions.containsNoNullElements(engines, "TestEngine array must not contain null elements");
			Collections.addAll(this.engines, engines);
			return this;
		}

		/**
		 * Add all of the supplied {@code listeners} to the configuration.
		 *
		 * @param listeners additional test execution listeners to register;
		 * never {@code null} or containing {@code null}
		 * @return this builder for method chaining
		 */
		public Builder addTestExecutionListeners(TestExecutionListener... listeners) {
			Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
			Preconditions.containsNoNullElements(listeners,
				"TestExecutionListener array must not contain null elements");
			Collections.addAll(this.listeners, listeners);
			return this;
		}

		/**
		 * Build the {@link LauncherConfig} that has been configured via this
		 * builder.
		 */
		public LauncherConfig build() {
			return new DefaultLauncherConfig(this.engineAutoRegistrationEnabled, this.listenerAutoRegistrationEnabled,
				this.engines, this.listeners);
		}

	}

}
