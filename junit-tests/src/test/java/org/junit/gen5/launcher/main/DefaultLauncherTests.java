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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.EngineIdFilter.byEngineId;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.support.hierarchical.DummyTestDescriptor;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.PostDiscoveryFilterStub;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
class DefaultLauncherTests {

	@Test
	void discoverEmptyTestPlanWithoutAnyEngines() {
		DefaultLauncher launcher = createLauncher();

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	void constructLauncherWithMultipleTestEnginesWithDuplicateIds() {
		DefaultLauncher launcher = createLauncher(new DummyTestEngine(), new DummyTestEngine());

		JUnitException exception = expectThrows(JUnitException.class,
			() -> launcher.discover(request().select(forUniqueId("foo")).build()));

		assertThat(exception).hasMessageContaining("multiple engines with the same ID");
	}

	@Test
	void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		DefaultLauncher launcher = createLauncher(new DummyTestEngine());

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	void discoverTestPlanForSingleEngine() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		engine.addTest("test1", noOp());
		engine.addTest("test2", noOp());

		DefaultLauncher launcher = createLauncher(engine);

		TestPlan testPlan = launcher.discover(request().select(PackageSelector.forPackageName("any")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(2);
		assertThat(testPlan.getChildren(new TestId("myEngine"))).hasSize(2);
	}

	@Test
	void discoverTestPlanForMultipleEngines() {
		DummyTestEngine firstEngine = new DummyTestEngine("engine1");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("engine2");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		DefaultLauncher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).build());

		assertThat(testPlan.getRoots()).hasSize(2);
		assertThat(testPlan.getChildren(new TestId("engine1"))).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("engine2"))).hasSize(1);
	}

	@Test
	void launcherWillNotCallEnginesThatAreFilteredByAnEngineIdFilter() {
		DummyTestEngine firstEngine = new DummyTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		DefaultLauncher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).filter(
				byEngineId("first")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("first"))).hasSize(1);
	}

	@Test
	void launcherAppliesPostDiscoveryFilters() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		DummyTestDescriptor test1 = engine.addTest("test1", noOp());
		engine.addTest("test2", noOp());

		DefaultLauncher launcher = createLauncher(engine);

		PostDiscoveryFilter includeWithUniqueIdContainsTest = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().contains("test")), () -> "filter1");
		PostDiscoveryFilter includeWithUniqueIdContains1 = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().contains("1")), () -> "filter2");

		TestPlan testPlan = launcher.discover( //
			request() //
					.select(PackageSelector.forPackageName("any")) //
					.filter(includeWithUniqueIdContainsTest, includeWithUniqueIdContains1) //
					.build());

		assertThat(testPlan.getChildren(new TestId("myEngine"))).hasSize(1);
		assertThat(testPlan.getTestIdentifier(new TestId(test1.getUniqueId()))).isNotNull();
	}

	private static Runnable noOp() {
		return () -> {
		};
	}
}
