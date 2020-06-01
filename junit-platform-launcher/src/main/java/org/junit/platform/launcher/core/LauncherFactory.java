/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.launcher.LauncherConstants.DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ClassNamePatternFilterUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Factory for creating {@link Launcher} instances by invoking {@link #create()}
 * or {@link #create(LauncherConfig)}.
 *
 * <p>By default, test engines are discovered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} mechanism. For that purpose, a
 * text file named {@code META-INF/services/org.junit.platform.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.platform.engine.TestEngine}
 * interface is declared.
 *
 * <p>By default, test execution listeners are discovered at runtime via the
 * {@link java.util.ServiceLoader ServiceLoader} mechanism and are
 * automatically registered with the {@link Launcher} created by this factory.
 * Users may register additional listeners using the
 * {@link Launcher#registerTestExecutionListeners(TestExecutionListener...)}
 * method on the created launcher instance.
 *
 * <p>For full control over automatic registration and programmatic registration
 * of test engines and listeners, supply an instance of {@link LauncherConfig}
 * to {@link #create(LauncherConfig)}.
 *
 * @since 1.0
 * @see Launcher
 * @see LauncherConfig
 */
@API(status = STABLE, since = "1.0")
public class LauncherFactory {

	private LauncherFactory() {
		/* no-op */
	}

	/**
	 * Factory method for creating a new {@link Launcher} using automatic
	 * registration for test engines and test execution listeners.
	 *
	 * @throws PreconditionViolationException if no test engines are detected
	 * @see #create(LauncherConfig)
	 */
	public static Launcher create() throws PreconditionViolationException {
		return create(LauncherConfig.builder().build());
	}

	/**
	 * Factory method for creating a new {@link Launcher} using the supplied
	 * {@link LauncherConfig}.
	 *
	 * @param config the configuration for the launcher; never {@code null}
	 * @throws PreconditionViolationException if the supplied launcher
	 * configuration is {@code null}, or if no test engines are detected or
	 * registered
	 * @see #create()
	 * @since 1.3
	 */
	@API(status = EXPERIMENTAL, since = "1.3")
	public static Launcher create(LauncherConfig config) throws PreconditionViolationException {
		Preconditions.notNull(config, "LauncherConfig must not be null");

		Set<TestEngine> engines = new LinkedHashSet<>();
		if (config.isTestEngineAutoRegistrationEnabled()) {
			new ServiceLoaderTestEngineRegistry().loadTestEngines().forEach(engines::add);
		}
		engines.addAll(config.getAdditionalTestEngines());

		List<PostDiscoveryFilter> filters = new ArrayList<>();
		if (config.isPostDiscoveryFilterAutoRegistrationEnabled()) {
			new ServiceLoaderPostDiscoveryFilterRegistry().loadPostDiscoveryFilters().forEach(filters::add);
		}
		filters.addAll(config.getAdditionalPostDiscoveryFilters());

		Launcher launcher = new DefaultLauncher(engines, filters);

		if (config.isTestExecutionListenerAutoRegistrationEnabled()) {
			loadAndFilterTestExecutionListeners().forEach(launcher::registerTestExecutionListeners);
		}
		config.getAdditionalTestExecutionListeners().forEach(launcher::registerTestExecutionListeners);

		return launcher;
	}

	private static Stream<TestExecutionListener> loadAndFilterTestExecutionListeners() {
		Iterable<TestExecutionListener> listeners = new ServiceLoaderTestExecutionListenerRegistry().loadListeners();
		ConfigurationParameters configurationParameters = LauncherConfigurationParameters.builder().build();
		String deactivatedListenersPattern = configurationParameters.get(
			DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME).orElse(null);
		// @formatter:off
		return StreamSupport.stream(listeners.spliterator(), false)
				.filter(ClassNamePatternFilterUtils.excludeMatchingClasses(deactivatedListenersPattern));
		// @formatter:on
	}

}
