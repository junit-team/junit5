/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.suite.engine.testcases.Simple;
import org.junit.platform.suite.engine.testsuites.CyclicSuiteSuite;
import org.junit.platform.suite.engine.testsuites.SelectClassesSuite;

class SuiteTestDescriptorTest {

	UniqueId engineId = UniqueId.forEngine(SuiteEngineDescriptor.ENGINE_ID);
	UniqueId suiteId = engineId.append(SuiteTestDescriptor.SEGMENT_TYPE, "test");
	UniqueId jupiterEngineId = suiteId.append("engine", JupiterEngineDescriptor.ENGINE_ID);
	UniqueId suiteEngineId = suiteId.append("engine", SuiteEngineDescriptor.ENGINE_ID);
	UniqueId testClassId = jupiterEngineId.append(ClassTestDescriptor.SEGMENT_TYPE, Simple.class.getName());
	UniqueId methodId = testClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

	// @formatter:off
	UniqueId methodIdInTestPlan = UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID)
			.append(ClassTestDescriptor.SEGMENT_TYPE, Simple.class.getName())
			.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");
	// @formatter:on

	ConfigurationParameters parameters = new EmptyConfigurationParameters();
	SuiteConfiguration configuration = new SuiteConfiguration(parameters);
	SuiteTestDescriptor suite = new SuiteTestDescriptor(suiteId, Object.class, configuration);

	@Test
	void suiteIsEmptyBeforeDiscovery() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		assertEquals(emptySet(), suite.getChildren());
	}

	@Test
	void suitDiscoversTestsFromClass() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();
		assertEquals(Set.of(suiteEngineId, jupiterEngineId, testClassId, methodId),
			suite.getDescendants().stream().map(TestDescriptor::getUniqueId).collect(toSet()));
	}

	@Test
	void suitDiscoversTestsFromUniqueId() {
		suite.addDiscoveryRequestFrom(methodIdInTestPlan);
		suite.discover();
		assertEquals(Set.of(suiteEngineId, jupiterEngineId, testClassId, methodId),
			suite.getDescendants().stream().map(TestDescriptor::getUniqueId).collect(toSet()));
	}

	@Test
	void suiteCanOnlyDiscoverOnce() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, suite::discover);
		assertEquals("discovery can only happen once", exception.getMessage());
	}

	@Test
	void discoveryPlanCanNotBeModifiedAfterDiscovery() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();
		assertAll(() -> {
			PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
				() -> suite.addDiscoveryRequestFrom(SelectClassesSuite.class));
			assertEquals("discovery request can not be modified after discovery", exception.getMessage());

		}, () -> {
			PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
				() -> suite.addDiscoveryRequestFrom(methodIdInTestPlan));
			assertEquals("discovery request can not be modified after discovery", exception.getMessage());
		});
	}

	@Test
	void uniqueIdInSuite() {
		TestIdentifier from = createTestIdentifier(methodIdInTestPlan);
		assertEquals(methodId, suite.uniqueIdInSuite(from));
	}

	private TestIdentifier createTestIdentifier(UniqueId methodIdInTestPlan) {
		return TestIdentifier.from(new AbstractTestDescriptor(methodIdInTestPlan, "test()") {
			@Override
			public Type getType() {
				return Type.TEST;
			}
		});
	}

	@Test
	void suitesMayNotContainACycle() {
		// @formatter:off
		UniqueId expectedCycle = suiteId
				.append("engine", SuiteEngineDescriptor.ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, CyclicSuiteSuite.class.getName())
				.append("engine", SuiteEngineDescriptor.ENGINE_ID)
				.append(SuiteTestDescriptor.SEGMENT_TYPE, CyclicSuiteSuite.class.getName());
		// @formatter:on
		suite.addDiscoveryRequestFrom(CyclicSuiteSuite.class);
		JUnitException exception = assertThrows(JUnitException.class, suite::discover);
		assertEquals("Configuration error: The suite configuration may not contain a cycle [" + expectedCycle + "]",
			exception.getCause().getCause().getCause().getMessage());
	}

}
