/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class LauncherFactoryTests {

	@Test
	void testCreate() {
		Launcher launcher = LauncherFactory.create();
		LauncherDiscoveryRequest discoveryRequest = this.createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		TestPlan testPlan = launcher.discover(discoveryRequest);
		Set<TestIdentifier> roots = testPlan.getRoots();
		assertThat(roots).hasSize(junitVintageEngineIsPresent() ? 2 : 1);

		// @formatter:off
		List<String> ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		if (junitVintageEngineIsPresent()) {
			assertThat(ids).containsOnly("[engine:junit-vintage]", "[engine:junit-jupiter]");
		}
		else {
			assertThat(ids).containsOnly("[engine:junit-jupiter]");
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

		@Test
		void testJ5() {
		}

	}

	private static boolean junitVintageEngineIsPresent() {
		return ReflectionUtils.loadClass("org.junit.vintage.engine.VintageTestEngine").isPresent();
	}

}
