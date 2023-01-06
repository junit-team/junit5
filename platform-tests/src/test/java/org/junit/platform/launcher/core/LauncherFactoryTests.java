/*
 * Copyright 2015-2023 the original author or authors.
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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestLauncherDiscoveryListener;
import org.junit.platform.launcher.TestLauncherSessionListener;
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
		withTestServices(() -> {
			var launcher = (InternalLauncher) LauncherFactory.create();
			var listeners = launcher.getTestExecutionListenerRegistry().getListeners();
			var listener = listeners.stream().filter(NoopTestExecutionListener.class::isInstance).findFirst();
			assertThat(listener).isPresent();
		});
	}

	@Test
	void unusedTestExecutionListenerIsNotLoadedViaServiceApi() {
		withTestServices(() -> {
			var launcher = (InternalLauncher) LauncherFactory.create();
			var listeners = launcher.getTestExecutionListenerRegistry().getListeners();

			assertThat(listeners).filteredOn(AnotherUnusedTestExecutionListener.class::isInstance).isEmpty();
			assertThat(listeners).filteredOn(UnusedTestExecutionListener.class::isInstance).isEmpty();
			assertThat(listeners).filteredOn(NoopTestExecutionListener.class::isInstance).isNotEmpty();
		});
	}

	@Test
	void create() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var testPlan = LauncherFactory.create().discover(discoveryRequest);
		var roots = testPlan.getRoots();
		assertThat(roots).hasSize(3);

		// @formatter:off
		var ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-vintage]", "[engine:junit-jupiter]",
			"[engine:junit-platform-suite]");
	}

	@Test
	void createWithConfig() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var config = LauncherConfig.builder()//
				.enableTestEngineAutoRegistration(false)//
				.addTestEngines(new JupiterTestEngine())//
				.build();

		var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		var roots = testPlan.getRoots();
		assertThat(roots).hasSize(1);

		// @formatter:off
		var ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-jupiter]");
	}

	@Test
	void createWithPostDiscoveryFilters() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var config = LauncherConfig.builder()//
				.addPostDiscoveryFilters(TagFilter.includeTags("test-post-discovery")).build();

		var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
		assertThat(vintage).isEmpty();

		final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
		assertThat(jupiter).hasSize(1);
	}

	@Test
	void applyPostDiscoveryFiltersViaServiceApi() {
		final var current = Thread.currentThread().getContextClassLoader();
		try {
			var url = getClass().getClassLoader().getResource("testservices/");
			var classLoader = new URLClassLoader(new URL[] { url }, current);
			Thread.currentThread().setContextClassLoader(classLoader);

			var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			var config = LauncherConfig.builder()//
					.build();

			var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
			assertThat(vintage).isEmpty();

			final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
			assertThat(jupiter).hasSize(1);
		}
		finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Test
	void notApplyIfDisabledPostDiscoveryFiltersViaServiceApi() {
		final var current = Thread.currentThread().getContextClassLoader();
		try {
			var url = getClass().getClassLoader().getResource("testservices/");
			var classLoader = new URLClassLoader(new URL[] { url }, current);
			Thread.currentThread().setContextClassLoader(classLoader);

			var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			var config = LauncherConfig.builder()//
					.enablePostDiscoveryFilterAutoRegistration(false).build();

			var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
			assertThat(vintage).hasSize(1);

			final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
			assertThat(jupiter).hasSize(1);
		}
		finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Test
	void doesNotDiscoverLauncherDiscoverRequestListenerViaServiceApiWhenDisabled() {
		withTestServices(() -> {
			var launcher = (InternalLauncher) LauncherFactory.create(
				LauncherConfig.builder().enableLauncherDiscoveryListenerAutoRegistration(false).build());
			var launcherDiscoveryListener = launcher.getLauncherDiscoveryListenerRegistry().getCompositeListener();

			assertThat(launcherDiscoveryListener).isSameAs(LauncherDiscoveryListener.NOOP);
		});
	}

	@Test
	void discoversLauncherDiscoverRequestListenerViaServiceApiByDefault() {
		withTestServices(() -> {
			var launcher = (InternalLauncher) LauncherFactory.create();
			var launcherDiscoveryListener = launcher.getLauncherDiscoveryListenerRegistry().getCompositeListener();

			assertThat(launcherDiscoveryListener.getClass().getSimpleName()).startsWith("Composite");
			assertThat(launcherDiscoveryListener).extracting("listeners").asList() //
					.contains(new TestLauncherDiscoveryListener());
		});
	}

	@Test
	void doesNotDiscoverLauncherSessionListenerViaServiceApiWhenDisabled() {
		withTestServices(() -> {
			var session = (DefaultLauncherSession) LauncherFactory.openSession(
				LauncherConfig.builder().enableLauncherSessionListenerAutoRegistration(false).build());

			assertThat(session.getListener()).isSameAs(LauncherSessionListener.NOOP);
		});
	}

	@Test
	void discoversLauncherSessionListenerViaServiceApiByDefault() {
		withTestServices(() -> {
			var session = (DefaultLauncherSession) LauncherFactory.openSession();

			assertThat(session.getListener()).isEqualTo(new TestLauncherSessionListener());
		});
	}

	private static void withTestServices(Runnable runnable) {
		var current = Thread.currentThread().getContextClassLoader();
		try {
			var url = LauncherFactoryTests.class.getClassLoader().getResource("testservices/");
			var classLoader = new URLClassLoader(new URL[] { url }, current);
			Thread.currentThread().setContextClassLoader(classLoader);
			runnable.run();
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
