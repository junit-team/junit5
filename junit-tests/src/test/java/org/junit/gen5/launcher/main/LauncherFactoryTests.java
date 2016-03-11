/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.gen5.engine.discovery.ClassSelector.*;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.*;

import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.launcher.*;

class LauncherFactoryTests {

	@Test
	void testCreate() {
		Launcher launcher = LauncherFactory.create();
		TestDiscoveryRequest discoveryRequest = this.createTestDiscoveryRequestForBothStandardEngineExampleClasses();

		TestPlan testPlan = launcher.discover(discoveryRequest);
		Set<TestIdentifier> roots = testPlan.getRoots();
		assertThat(roots).hasSize(2);

		// @formatter:off
		List<String> ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.map(TestId::toString)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit4]", "[engine:junit5]");
	}

	private TestDiscoveryRequest createTestDiscoveryRequestForBothStandardEngineExampleClasses() {
		// @formatter:off
		return request()
				.select(forClass(JUnit4Example.class))
				.select(forClass(JUnit5Example.class))
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
