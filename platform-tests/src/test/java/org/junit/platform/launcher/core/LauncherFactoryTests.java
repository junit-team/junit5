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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.NoopTestExecutionListener;

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

		@Test
		void testJ5() {
		}

	}

}
