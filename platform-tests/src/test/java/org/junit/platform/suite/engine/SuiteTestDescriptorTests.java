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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

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

	final UniqueId engineId = UniqueId.forEngine(SuiteEngineDescriptor.ENGINE_ID);
	final UniqueId suiteId = engineId.append(SuiteTestDescriptor.SEGMENT_TYPE, "test");
	final UniqueId jupiterEngineId = suiteId.append("engine", JupiterEngineDescriptor.ENGINE_ID);
	final UniqueId testClassId = jupiterEngineId.append(ClassTestDescriptor.SEGMENT_TYPE,
		SingleTestTestCase.class.getName());
	final UniqueId methodId = testClassId.append(TestMethodTestDescriptor.SEGMENT_TYPE, "test()");

	final ConfigurationParameters configurationParameters = new EmptyConfigurationParameters();
	final SuiteTestDescriptor suite = new SuiteTestDescriptor(suiteId, TestSuite.class, configurationParameters);

	@Test
	void suiteIsEmptyBeforeDiscovery() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);

		assertThat(suite.getChildren()).isEmpty();
	}

	@Test
	void suiteDiscoversTestsFromClass() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();

		assertThat(suite.getDescendants()).map(TestDescriptor::getUniqueId)//
				.containsExactly(jupiterEngineId, testClassId, methodId);
	}

	@Test
	void suitDiscoversTestsFromUniqueId() {
		suite.addDiscoveryRequestFrom(methodId);
		suite.discover();

		assertThat(suite.getDescendants()).map(TestDescriptor::getUniqueId)//
				.containsExactly(jupiterEngineId, testClassId, methodId);
	}

	@Test
	void discoveryPlanCanNotBeModifiedAfterDiscovery() {
		suite.addDiscoveryRequestFrom(SelectClassesSuite.class);
		suite.discover();

		assertAll(//
			() -> assertThatExceptionOfType(PreconditionViolationException.class)//
					.isThrownBy(() -> suite.addDiscoveryRequestFrom(SelectClassesSuite.class))//
					.withMessage("discovery request cannot be modified after discovery"),
			() -> assertThatExceptionOfType(PreconditionViolationException.class)//
					.isThrownBy(() -> suite.addDiscoveryRequestFrom(methodId))//
					.withMessage("discovery request cannot be modified after discovery"));
	}

	@Test
	void suiteMayRegisterTests() {
		assertThat(suite.mayRegisterTests()).isTrue();
	}

	@Suite
	static class TestSuite {
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
