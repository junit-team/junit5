/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.junit.vintage.engine.discovery.RunnerTestDescriptorAwareFilter.adapter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.vintage.engine.VintageUniqueIdBuilder;
import org.junit.vintage.engine.samples.junit4.IgnoredJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods;

/**
 * Tests for {@link TestClassRequestResolver}.
 *
 * @since 4.12
 */
@TrackLogRecords
class TestClassRequestResolverTests {

	@Test
	void doesNotLogAnythingForFilterableRunner(LogRecordListener listener) {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		RunnerTestDescriptorAwareFilter filter = adapter(
			matchMethodDescription(createTestDescription(testClass, "failingTest")));

		resolve(new TestClassRequest(testClass, asList(filter)));

		assertThat(listener.stream(TestClassRequestResolver.class)).isEmpty();
	}

	@Test
	void doesNotLogAnythingForNonFilterableRunnerIfNoFiltersAreToBeApplied(LogRecordListener listener) {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		List<RunnerTestDescriptorAwareFilter> filters = emptyList();

		resolve(new TestClassRequest(testClass, filters));

		assertThat(listener.stream(TestClassRequestResolver.class)).isEmpty();
	}

	@Test
	void logsWarningOnNonFilterableRunner(LogRecordListener listener) {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		RunnerTestDescriptorAwareFilter filter = adapter(
			matchMethodDescription(createTestDescription(testClass, "test")));

		resolve(new TestClassRequest(testClass, asList(filter)));

		// @formatter:off
		assertThat(listener.stream(TestClassRequestResolver.class, Level.WARNING)
			.map(LogRecord::getMessage)
			.filter(m -> m.equals("Runner " + IgnoredClassRunner.class.getName() //
				+ " (used on " + testClass.getName() + ") does not support filtering" //
				+ " and will therefore be run completely."))
			.count()
		).isEqualTo(1);
		// @formatter:on
	}

	private void resolve(TestClassRequest request) {
		TestClassRequestResolver resolver = new TestClassRequestResolver();
		resolver.createRunnerTestDescriptor(request, VintageUniqueIdBuilder.engineId());
	}

}
