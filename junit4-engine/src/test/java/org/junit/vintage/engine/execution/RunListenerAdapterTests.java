/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.vintage.engine.JUnit4UniqueIdBuilder.engineId;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.vintage.engine.RecordCollectingLogger;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails;

/**
 * @since 5.0
 */
class RunListenerAdapterTests {

	@Test
	void logsUnknownDescriptions() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		RecordCollectingLogger logger = new RecordCollectingLogger();
		EngineDescriptor engineDescriptor = new EngineDescriptor(engineId(), "JUnit 4");
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(engineDescriptor, testClass,
			new BlockJUnit4ClassRunner(testClass));

		TestRun testRun = new TestRun(runnerTestDescriptor, logger);

		Description unknownDescription = createTestDescription(testClass, "doesNotExist");
		Optional<? extends TestDescriptor> testDescriptor = testRun.lookupTestDescriptor(unknownDescription);

		assertThat(testDescriptor).isEmpty();
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals("Runner " + BlockJUnit4ClassRunner.class.getName() + " on class " + testClass.getName()
				+ " reported event for unknown Description: doesNotExist(" + testClass.getName()
				+ "). It will be ignored.",
			logRecord.getMessage());
	}

}
