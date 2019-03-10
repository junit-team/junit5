/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.DemoMethodTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
class ExecutionListenerAdapterTests {
	private TestDescriptor testDescriptor;

	private ExecutionListenerAdapter adapter;

	@BeforeEach
	void setUp() {
		testDescriptor = getSampleMethodTestDescriptor();
		InternalTestPlan testPlan = getInternalTestPlan(testDescriptor);
		ThrowingTestExecutionListener testExecutionListener = new ThrowingTestExecutionListener();

		adapter = new ExecutionListenerAdapter(testPlan, testExecutionListener);
	}

	@Test
	void testReportingEntryPublished() {
		TestDescriptor testDescriptor = getSampleMethodTestDescriptor();

		//cannot mock final classes with mockito
		InternalTestPlan testPlan = getInternalTestPlan(testDescriptor);
		TestIdentifier testIdentifier = testPlan.getTestIdentifier(testDescriptor.getUniqueId().toString());

		//not yet spyable with mockito? -> https://github.com/mockito/mockito/issues/146
		MockTestExecutionListener testExecutionListener = new MockTestExecutionListener();
		ExecutionListenerAdapter executionListenerAdapter = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);

		ReportEntry entry = ReportEntry.from("one", "two");
		executionListenerAdapter.reportingEntryPublished(testDescriptor, entry);

		assertThat(testExecutionListener.entry).isEqualTo(entry);
		assertThat(testExecutionListener.testIdentifier).isEqualTo(testIdentifier);
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfDynamicTestRegisteredListenerMethodFails(LogRecordListener logRecordListener) {
		adapter.dynamicTestRegistered(testDescriptor);

		assertThatTestListenerErrorLogged(logRecordListener, "dynamicTestRegistered");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionStartedListenerMethodFails(LogRecordListener logRecordListener) {
		adapter.executionStarted(testDescriptor);

		assertThatTestListenerErrorLogged(logRecordListener, "executionStarted");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionSkippedListenerMethodFails(LogRecordListener logRecordListener) {
		adapter.executionSkipped(testDescriptor, "deliberately skipped container");

		assertThatTestListenerErrorLogged(logRecordListener, "executionSkipped");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfExecutionFinishedListenerMethodFails(LogRecordListener logRecordListener) {
		adapter.executionFinished(testDescriptor, mock(TestExecutionResult.class));

		assertThatTestListenerErrorLogged(logRecordListener, "executionFinished");
	}

	@Test
	@TrackLogRecords
	void shouldNotThrowExceptionButLogIfReportingEntryPublishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		adapter.reportingEntryPublished(testDescriptor, ReportEntry.from("one", "two"));

		assertThatTestListenerErrorLogged(logRecordListener, "reportingEntryPublished");
	}

	@Test
	@TrackLogRecords
	void shouldOutOfMemoryExceptionAndStopListenerWithoutLog(LogRecordListener logRecordListener) {
		InternalTestPlan testPlan = getInternalTestPlan(testDescriptor);
		TestExecutionListener testExecutionListener = new TestExecutionListener() {
			@Override
			public void executionStarted(TestIdentifier testIdentifier) {
				throw new OutOfMemoryError();
			}
		};

		ExecutionListenerAdapter adapter = new ExecutionListenerAdapter(testPlan, testExecutionListener);
		assertThatThrownBy(() -> adapter.executionStarted(testDescriptor)).isInstanceOf(OutOfMemoryError.class);

		assertNotLogs(logRecordListener);
	}

	private InternalTestPlan getInternalTestPlan(TestDescriptor testDescriptor) {
		Root root = new Root(null);
		root.add(mock(TestEngine.class), testDescriptor);
		return InternalTestPlan.from(root);
	}

	private LogRecord firstWarnLogRecord(LogRecordListener logRecordListener) throws AssertionError {
		return logRecordListener.stream(ExecutionListenerAdapter.class, Level.WARNING).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find error log record"));
	}

	private void assertNotLogs(LogRecordListener logRecordListener) throws AssertionError {
		assertThat(logRecordListener.stream(ExecutionListenerAdapter.class, Level.WARNING).count()).isZero();
	}

	private TestDescriptor getSampleMethodTestDescriptor() {
		Method localMethodNamedNothing = ReflectionUtils.findMethod(this.getClass(), "nothing", new Class<?>[0]).get();
		return new DemoMethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(),
			localMethodNamedNothing);
	}

	private void assertThatTestListenerErrorLogged(LogRecordListener logRecordListener, final String methodName) {
		assertThat(firstWarnLogRecord(logRecordListener).getMessage()).isEqualTo(
			"Failed to invoke ExecutionListener [org.junit.platform.launcher.core.ThrowingTestExecutionListener] for method ["
					+ methodName + "] with test display name [nothing()]");
	}

	//for reflection purposes only
	void nothing() {
	}

	static class MockTestExecutionListener implements TestExecutionListener {

		public TestIdentifier testIdentifier;
		public ReportEntry entry;

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			this.testIdentifier = testIdentifier;
			this.entry = entry;
		}

	}
}
