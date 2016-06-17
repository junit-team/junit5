/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.junit4.discovery.RunnerTestDescriptorAwareFilter.adapter;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit4.JUnit4UniqueIdBuilder;
import org.junit.gen5.engine.junit4.RecordCollectingLogger;
import org.junit.gen5.engine.junit4.samples.junit4.IgnoredJUnit4TestCase;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.internal.builders.IgnoredClassRunner;

/**
 * @since 5.0
 */
class TestClassRequestResolverTests {

	@Test
	void doesNotLogAnythingForFilterableRunner() {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		RunnerTestDescriptorAwareFilter filter = adapter(
			matchMethodDescription(createTestDescription(testClass, "failingTest")));

		List<LogRecord> logRecords = resolve(new TestClassRequest(testClass, asList(filter)));

		assertThat(logRecords).isEmpty();
	}

	@Test
	void doesNotLogAnythingForNonFilterableRunnerIfNoFiltersAreToBeApplied() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		List<RunnerTestDescriptorAwareFilter> filters = emptyList();

		List<LogRecord> logRecords = resolve(new TestClassRequest(testClass, filters));

		assertThat(logRecords).isEmpty();
	}

	@Test
	void logsWarningOnNonFilterableRunner() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		RunnerTestDescriptorAwareFilter filter = adapter(
			matchMethodDescription(createTestDescription(testClass, "test")));

		List<LogRecord> logRecords = resolve(new TestClassRequest(testClass, asList(filter)));

		assertThat(logRecords).hasSize(1);

		LogRecord logRecord = logRecords.get(0);
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals("Runner " + IgnoredClassRunner.class.getName() //
				+ " (used on " + testClass.getName() + ") does not support filtering" //
				+ " and will therefore be run completely.",
			logRecord.getMessage());
	}

	private List<LogRecord> resolve(TestClassRequest request) {
		TestDescriptor engineDescriptor = new EngineDescriptor(JUnit4UniqueIdBuilder.engineId(), "JUnit 4");
		RecordCollectingLogger logger = new RecordCollectingLogger();

		TestClassRequestResolver resolver = new TestClassRequestResolver(engineDescriptor, logger);
		resolver.populateEngineDescriptorFrom(singleton(request));

		return logger.getLogRecords();
	}

}
