/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.engine.testcases.SingleTestTestCase;
import org.junit.platform.suite.engine.testsuites.SelectClassesSuite;

/**
 * @since 1.8
 */
class SuiteTestDescriptorTests {
	@Suite
	static class TestSuite {

	}
	UniqueId engineId = UniqueId.forEngine(SuiteEngineDescriptor.ENGINE_ID);
	UniqueId suiteId = engineId.append(SuiteTestDescriptor.SEGMENT_TYPE, "test");
	UniqueId jupiterEngineId = suiteId.append("engine", JupiterEngineDescriptor.ENGINE_ID);
	UniqueId testClassId = jupiterEngineId.append(ClassTestDescriptor.SEGMENT_TYPE, SingleTestTestCase.class.getName());
	UniqueId methodId = testClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

	ConfigurationParameters configurationParameters = new EmptyConfigurationParameters();
	SuiteTestDescriptor suite = new SuiteTestDescriptor(suiteId, TestSuite.class, configurationParameters);

	@Test
	void suiteIsEmptyBeforeDiscovery() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		assertEquals(emptySet(), suite.getChildren());
	}

	@Test
	void suiteDiscoversTestsFromClass() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();
		assertEquals(Set.of(jupiterEngineId, testClassId, methodId),
			suite.getDescendants().stream().map(TestDescriptor::getUniqueId).collect(toSet()));
	}

	@Test
	void suitDiscoversTestsFromUniqueId() {
		suite.addDiscoveryRequestFrom(methodId);
		suite.discover();
		assertEquals(Set.of(jupiterEngineId, testClassId, methodId),
			suite.getDescendants().stream().map(TestDescriptor::getUniqueId).collect(toSet()));
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
				() -> suite.addDiscoveryRequestFrom(methodId));
			assertEquals("discovery request can not be modified after discovery", exception.getMessage());
		});
	}

	@Test
	void suiteMayRegisterTests() {
		assertTrue(suite.mayRegisterTests());
	}

	private static class EmptyConfigurationParameters implements ConfigurationParameters {
		@Override
		public Optional<String> get(String key) {
			return Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return Optional.empty();
		}

		@Override
		@SuppressWarnings("deprecation")
		public int size() {
			return 0;
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}

	}

}
