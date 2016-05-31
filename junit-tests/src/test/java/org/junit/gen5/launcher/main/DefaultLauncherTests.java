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
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.selectUniqueId;
import static org.junit.gen5.launcher.EngineIdFilter.requireEngineId;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.Optional;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.ConfigurationParameters;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.stubs.TestEngineSpy;
import org.junit.gen5.engine.support.hierarchical.DummyTestDescriptor;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.PostDiscoveryFilterStub;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
class DefaultLauncherTests {

	private static final String FOO = DefaultLauncherTests.class.getSimpleName() + ".foo";
	private static final String BAR = DefaultLauncherTests.class.getSimpleName() + ".bar";

	private static final Runnable noOp = () -> {
	};

	@Test
	void constructLauncherWithoutAnyEngines() {
		Throwable exception = expectThrows(PreconditionViolationException.class, () -> createLauncher());

		assertThat(exception).hasMessageContaining("Cannot create Launcher without at least one TestEngine");
	}

	@Test
	void constructLauncherWithMultipleTestEnginesWithDuplicateIds() {
		JUnitException exception = expectThrows(JUnitException.class,
			() -> createLauncher(new DummyTestEngine("dummy id"), new DummyTestEngine("dummy id")));

		assertThat(exception).hasMessageContaining("multiple engines with the same ID");
	}

	@Test
	void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		DefaultLauncher launcher = createLauncher(new DummyTestEngine());

		TestPlan testPlan = launcher.discover(request().build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	void discoverTestPlanForSingleEngine() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		engine.addTest("test1", noOp);
		engine.addTest("test2", noOp);

		DefaultLauncher launcher = createLauncher(engine);

		TestPlan testPlan = launcher.discover(request().selectors(PackageSelector.selectPackage("any")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(2);
		assertThat(testPlan.getChildren("[engine:myEngine]")).hasSize(2);
	}

	@Test
	void discoverTestPlanForMultipleEngines() {
		DummyTestEngine firstEngine = new DummyTestEngine("engine1");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		DummyTestEngine secondEngine = new DummyTestEngine("engine2");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		DefaultLauncher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId())).build());

		assertThat(testPlan.getRoots()).hasSize(2);
		assertThat(testPlan.getChildren(UniqueId.forEngine("engine1").toString())).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("engine2").toString())).hasSize(1);
	}

	@Test
	void launcherWillNotCallEnginesThatAreFilteredByAnEngineIdFilter() {
		DummyTestEngine firstEngine = new DummyTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		DummyTestEngine secondEngine = new DummyTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		DefaultLauncher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId())).filters(
				requireEngineId("first")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("first").toString())).hasSize(1);
	}

	@Test
	void launcherWillNotExecuteAnyEnginesWithMultipleEngineIdFilters() {
		DummyTestEngine firstEngine = new DummyTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		DummyTestEngine secondEngine = new DummyTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		DefaultLauncher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId())).filters(
				requireEngineId("first"), requireEngineId("second")).build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	void launcherAppliesPostDiscoveryFilters() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		DummyTestDescriptor test1 = engine.addTest("test1", noOp);
		engine.addTest("test2", noOp);

		DefaultLauncher launcher = createLauncher(engine);

		PostDiscoveryFilter includeWithUniqueIdContainsTest = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().toString().contains("test")),
			() -> "filter1");
		PostDiscoveryFilter includeWithUniqueIdContains1 = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().toString().contains("1")), () -> "filter2");

		TestPlan testPlan = launcher.discover( //
			request() //
					.selectors(PackageSelector.selectPackage("any")) //
					.filters(includeWithUniqueIdContainsTest, includeWithUniqueIdContains1) //
					.build());

		assertThat(testPlan.getChildren(UniqueId.forEngine("myEngine").toString())).hasSize(1);
		assertThat(testPlan.getTestIdentifier(test1.getUniqueId().toString())).isNotNull();
	}

	@Test
	void withoutConfigurationParameters_launcherPassesEmptyConfigurationParametersIntoTheExecutionRequest() {
		TestEngineSpy engine = new TestEngineSpy();

		DefaultLauncher launcher = createLauncher(engine);
		launcher.execute(request().build());

		ConfigurationParameters configurationParameters = engine.requestForExecution.getConfigurationParameters();
		assertThat(configurationParameters.get("key").isPresent()).isFalse();
		assertThat(configurationParameters.size()).isEqualTo(0);
	}

	@Test
	void withConfigurationParameters_launcherPassesPopulatedConfigurationParametersIntoTheExecutionRequest() {
		TestEngineSpy engine = new TestEngineSpy();

		DefaultLauncher launcher = createLauncher(engine);
		launcher.execute(request().configurationParameter("key", "value").build());

		ConfigurationParameters configurationParameters = engine.requestForExecution.getConfigurationParameters();
		assertThat(configurationParameters.size()).isEqualTo(1);
		assertThat(configurationParameters.get("key").isPresent()).isTrue();
		assertThat(configurationParameters.get("key").get()).isEqualTo("value");
	}

	@Test
	void withoutConfigurationParameters_LookupFallsBackToSystemProperty() {
		System.setProperty(FOO, BAR);

		try {
			TestEngineSpy engine = new TestEngineSpy();

			DefaultLauncher launcher = createLauncher(engine);
			launcher.execute(request().build());

			ConfigurationParameters configurationParameters = engine.requestForExecution.getConfigurationParameters();
			assertThat(configurationParameters.size()).isEqualTo(0);
			Optional<String> optionalFoo = configurationParameters.get(FOO);
			assertTrue(optionalFoo.isPresent(), "foo should have been picked up via system property");
			assertEquals(BAR, optionalFoo.get(), "foo property");
		}
		finally {
			System.clearProperty(FOO);
		}
	}

}
