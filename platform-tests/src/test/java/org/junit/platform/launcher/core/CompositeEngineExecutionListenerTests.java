/*
 * Copyright 2015-2023 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.DemoMethodTestDescriptor;

@TrackLogRecords
class CompositeEngineExecutionListenerTests {

	private final List<EngineExecutionListener> listeners = new ArrayList<>(
		List.of(new ThrowingEngineExecutionListener()));

	@Test
	void shouldNotThrowExceptionButLogIfDynamicTestRegisteredListenerMethodFails(LogRecordListener logRecordListener) {
		var testDescriptor = getSampleMethodTestDescriptor();

		compositeTestExecutionListener().dynamicTestRegistered(testDescriptor);

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEngineExecutionListener.class,
			"dynamicTestRegistered");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionStartedListenerMethodFails(LogRecordListener logRecordListener) {
		var testDescriptor = getSampleMethodTestDescriptor();

		compositeTestExecutionListener().executionStarted(testDescriptor);

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEngineExecutionListener.class, "executionStarted");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionSkippedListenerMethodFails(LogRecordListener logRecordListener) {
		var testDescriptor = getSampleMethodTestDescriptor();

		compositeTestExecutionListener().executionSkipped(testDescriptor, "deliberately skipped container");

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEngineExecutionListener.class, "executionSkipped");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionFinishedListenerMethodFails(LogRecordListener logRecordListener) {
		var testDescriptor = getSampleMethodTestDescriptor();

		compositeTestExecutionListener().executionFinished(testDescriptor, mock(TestExecutionResult.class));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEngineExecutionListener.class,
			"executionFinished");
	}

	@Test
	void shouldNotThrowExceptionButLogIfReportingEntryPublishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		var testDescriptor = getSampleMethodTestDescriptor();

		compositeTestExecutionListener().reportingEntryPublished(testDescriptor, ReportEntry.from("one", "two"));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEngineExecutionListener.class,
			"reportingEntryPublished");
	}

	@Test
	void shouldThrowOutOfMemoryExceptionAndStopListenerWithoutLog(LogRecordListener logRecordListener) {
		listeners.clear();
		listeners.add(new EngineExecutionListener() {
			@Override
			public void executionStarted(TestDescriptor testDescriptor) {
				throw new OutOfMemoryError();
			}
		});
		var testDescriptor = getSampleMethodTestDescriptor();
		assertThatThrownBy(() -> compositeTestExecutionListener().executionStarted(testDescriptor)).isInstanceOf(
			OutOfMemoryError.class);

		assertNotLogs(logRecordListener);
	}

	private EngineExecutionListener compositeTestExecutionListener() {
		return new CompositeEngineExecutionListener(listeners);
	}

	private LogRecord firstWarnLogRecord(LogRecordListener logRecordListener) throws AssertionError {
		return logRecordListener.stream(CompositeEngineExecutionListener.class, Level.WARNING).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find error log record"));
	}

	private void assertNotLogs(LogRecordListener logRecordListener) throws AssertionError {
		assertThat(logRecordListener.stream(CompositeEngineExecutionListener.class, Level.WARNING).count()).isZero();
	}

	private TestDescriptor getSampleMethodTestDescriptor() {
		return getDemoMethodTestDescriptor();
	}

	private void assertThatTestListenerErrorLogged(LogRecordListener logRecordListener, Class<?> listenerClass,
			String methodName) {
		assertThat(firstWarnLogRecord(logRecordListener).getMessage()).startsWith(
			"EngineExecutionListener [" + listenerClass.getName() + "] threw exception for method: " + methodName);
	}

	private DemoMethodTestDescriptor getDemoMethodTestDescriptor() {
		var method = ReflectionUtils.findMethod(this.getClass(), "getDemoMethodTestDescriptor",
			new Class<?>[0]).orElseThrow();
		return new DemoMethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(), method);
	}

	private static class ThrowingEngineExecutionListener implements EngineExecutionListener {

		@Override
		public void dynamicTestRegistered(TestDescriptor testDescriptor) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionStarted(TestDescriptor testDescriptor) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionSkipped(TestDescriptor testDescriptor, String reason) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
			throw new RuntimeException("failed to invoke listener");
		}
	}

}
