/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestEngine;

/**
 * @since 1.0
 */
class DefaultLauncherEngineFilterTests {

	private static final Runnable noOp = () -> {
	};

	@Test
	void launcherWillNotExecuteEnginesIfNotIncludedByAnEngineFilter() {
		var firstEngine = new DemoHierarchicalTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		var launcher = createLauncher(firstEngine, secondEngine);

		// @formatter:off
		var testPlan = launcher.discover(
			request()
				.selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId()))
				.filters(includeEngines("first"))
				.build());
		// @formatter:on

		assertThat(testPlan.getRoots()).hasSize(1);
		var rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("first").toString())).hasSize(1);
	}

	@Test
	void launcherWillExecuteAllEnginesExplicitlyIncludedViaSingleEngineFilter() {
		var firstEngine = new DemoHierarchicalTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		var launcher = createLauncher(firstEngine, secondEngine);

		// @formatter:off
		var testPlan = launcher.discover(
			request()
				.selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId()))
				.filters(includeEngines("first", "second"))
				.build());
		// @formatter:on

		assertThat(testPlan.getRoots()).hasSize(2);
	}

	@Test
	void launcherWillNotExecuteEnginesExplicitlyIncludedViaMultipleCompetingEngineFilters() {
		var firstEngine = new DemoHierarchicalTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		var launcher = createLauncher(firstEngine, secondEngine);

		// @formatter:off
		var testPlan = launcher.discover(
			request()
				.selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId()))
				.filters(includeEngines("first"), includeEngines("second"))
				.build());
		// @formatter:on

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	void launcherWillNotExecuteEnginesExplicitlyExcludedByAnEngineFilter() {
		var firstEngine = new DemoHierarchicalTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		var launcher = createLauncher(firstEngine, secondEngine);

		// @formatter:off
		var testPlan = launcher.discover(
			request()
				.selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId()))
				.filters(excludeEngines("second"))
				.build());
		// @formatter:on

		assertThat(testPlan.getRoots()).hasSize(1);
		var rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("first").toString())).hasSize(1);
	}

	@Test
	void launcherWillExecuteEnginesHonoringBothIncludeAndExcludeEngineFilters() {
		var firstEngine = new DemoHierarchicalTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);
		var thirdEngine = new DemoHierarchicalTestEngine("third");
		TestDescriptor test3 = thirdEngine.addTest("test3", noOp);

		var launcher = createLauncher(firstEngine, secondEngine, thirdEngine);

		// @formatter:off
		var testPlan = launcher.discover(
			request()
				.selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId()), selectUniqueId(test3.getUniqueId()))
				.filters(includeEngines("first", "second"), excludeEngines("second"))
				.build());
		// @formatter:on

		assertThat(testPlan.getRoots()).hasSize(1);
		var rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("first").toString())).hasSize(1);
	}

}
