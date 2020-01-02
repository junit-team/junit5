/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_DYNAMIC;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails;

/**
 * @since 4.12
 */
class TestRunTests {

	@Test
	void returnsEmptyOptionalForUnknownDescriptions() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		UniqueId runnerId = engineId().append(SEGMENT_TYPE_RUNNER, testClass.getName());
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(runnerId, testClass,
			new BlockJUnit4ClassRunner(testClass));
		Description unknownDescription = createTestDescription(testClass, "dynamicTest");

		TestRun testRun = new TestRun(runnerTestDescriptor);
		Optional<VintageTestDescriptor> testDescriptor = testRun.lookupNextTestDescriptor(unknownDescription);

		assertThat(testDescriptor).isEmpty();
	}

	@Test
	void registersDynamicTestDescriptors() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		UniqueId runnerId = engineId().append(SEGMENT_TYPE_RUNNER, testClass.getName());
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(runnerId, testClass,
			new BlockJUnit4ClassRunner(testClass));
		UniqueId dynamicTestId = runnerId.append(SEGMENT_TYPE_DYNAMIC, "dynamicTest");
		Description dynamicDescription = createTestDescription(testClass, "dynamicTest");
		VintageTestDescriptor dynamicTestDescriptor = new VintageTestDescriptor(dynamicTestId, dynamicDescription,
			null);

		TestRun testRun = new TestRun(runnerTestDescriptor);
		testRun.registerDynamicTest(dynamicTestDescriptor);

		assertThat(testRun.lookupNextTestDescriptor(dynamicDescription)).contains(dynamicTestDescriptor);
		assertTrue(testRun.isDescendantOfRunnerTestDescriptor(dynamicTestDescriptor));
	}

}
