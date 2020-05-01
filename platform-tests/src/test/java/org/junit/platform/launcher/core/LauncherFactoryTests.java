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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.AnotherUnusedTestExecutionListener;
import org.junit.platform.launcher.listeners.NoopTestExecutionListener;
import org.junit.platform.launcher.listeners.UnusedTestExecutionListener;

/**
 * @since 1.0
 */
class LauncherFactoryTests {

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class, () -> LauncherFactory.create(null));
	}

	@Test
	void noopTestExecutionListenerIsLoadedViaServiceApi() {
		DefaultLauncher launcher = (DefaultLauncher) LauncherFactory.create();
		List<TestExecutionListener> listeners = launcher.getTestExecutionListenerRegistry().getTestExecutionListeners();
		Optional<TestExecutionListener> listener = listeners.stream().filter(
			NoopTestExecutionListener.class::isInstance).findFirst();
		assertThat(listener).isPresent();
	}

	@Test
	void unusedTestExecutionListenerIsNotLoadedViaServiceApi() {
		DefaultLauncher launcher = (DefaultLauncher) LauncherFactory.create();
		List<TestExecutionListener> listeners = launcher.getTestExecutionListenerRegistry().getTestExecutionListeners();

		assertThat(listeners).filteredOn(AnotherUnusedTestExecutionListener.class::isInstance).isEmpty();
		assertThat(listeners).filteredOn(UnusedTestExecutionListener.class::isInstance).isEmpty();
		assertThat(listeners).filteredOn(NoopTestExecutionListener.class::isInstance).isNotEmpty();
	}

	@Test
	void create() {
		LauncherDiscoveryRequest discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		TestPlan testPlan = LauncherFactory.create().discover(discoveryRequest);
		Set<TestIdentifier> roots = testPlan.getRoots();
		assertThat(roots).hasSize(2);

		// @formatter:off
		List<String> ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-vintage]", "[engine:junit-jupiter]");
	}

	@Test
	void createWithConfig() {
		LauncherDiscoveryRequest discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		LauncherConfig config = LauncherConfig.builder()//
				.enableTestEngineAutoRegistration(false)//
				.addTestEngines(new JupiterTestEngine())//
				.build();

		TestPlan testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		Set<TestIdentifier> roots = testPlan.getRoots();
		assertThat(roots).hasSize(1);

		// @formatter:off
		List<String> ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-jupiter]");
	}

	@Test
	void createWithPostDiscoveryFilters() {
		LauncherDiscoveryRequest discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		LauncherConfig config = LauncherConfig.builder()//
				.addPostDiscoveryFilters(TagFilter.includeTags("test-post-discovery")).build();

		TestPlan testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		final Set<TestIdentifier> vintage = testPlan.getChildren("[engine:junit-vintage]");
		assertThat(vintage).isEmpty();

		final Set<TestIdentifier> jupiter = testPlan.getChildren("[engine:junit-jupiter]");
		assertThat(jupiter).hasSize(1);
	}

	@Test
	void applyPostDiscoveryFiltersViaServiceApi() {
		final ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			URL url = getClass().getClassLoader().getResource("testservices/");
			URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, current);
			Thread.currentThread().setContextClassLoader(classLoader);

			LauncherDiscoveryRequest discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			LauncherConfig config = LauncherConfig.builder()//
					.build();

			TestPlan testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final Set<TestIdentifier> vintage = testPlan.getChildren("[engine:junit-vintage]");
			assertThat(vintage).isEmpty();

			final Set<TestIdentifier> jupiter = testPlan.getChildren("[engine:junit-jupiter]");
			assertThat(jupiter).hasSize(1);
		}
		finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Test
	void notApplyIfDisabledPostDiscoveryFiltersViaServiceApi() {
		final ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			URL url = getClass().getClassLoader().getResource("testservices/");
			URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, current);
			Thread.currentThread().setContextClassLoader(classLoader);

			LauncherDiscoveryRequest discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			LauncherConfig config = LauncherConfig.builder()//
					.enablePostDiscoveryFilterAutoRegistration(false).build();

			TestPlan testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final Set<TestIdentifier> vintage = testPlan.getChildren("[engine:junit-vintage]");
			assertThat(vintage).hasSize(1);

			final Set<TestIdentifier> jupiter = testPlan.getChildren("[engine:junit-jupiter]");
			assertThat(jupiter).hasSize(1);
		}
		finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	private LauncherDiscoveryRequest createLauncherDiscoveryRequestForBothStandardEngineExampleClasses() {
		// @formatter:off
		return request()
				.selectors(selectClass(JUnit4Example.class))
				.selectors(selectClass(JUnit5Example.class))
				.build();
		// @formatter:on
	}

	public static class JUnit4Example {

		@org.junit.Test
		public void testJ4() {
		}

	}

	static class JUnit5Example {

		@Tag("test-post-discovery")
		@Test
		void testJ5() {
		}

	}
}
