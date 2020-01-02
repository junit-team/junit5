/*
 * Copyright 2015-2020 the original author or authors.
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
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.DemoMethodTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@TrackLogRecords
class TestExecutionListenerRegistryTests {

	private TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry();
	private TestExecutionListener compositeTestExecutionListener;

	@BeforeEach
	void setUp() {
		registry.registerListeners(new ThrowingTestExecutionListener());
		compositeTestExecutionListener = registry.getCompositeTestExecutionListener();
	}

	@Test
	void shouldNotThrowExceptionButLogIfDynamicTestRegisteredListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.dynamicTestRegistered(testIdentifier);

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class,
			"dynamicTestRegistered");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionStartedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionStarted(testIdentifier);

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class, "executionStarted");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionSkippedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionSkipped(testIdentifier, "deliberately skipped container");

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class, "executionSkipped");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionFinishedListenerMethodFails(LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.executionFinished(testIdentifier, mock(TestExecutionResult.class));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class, "executionFinished");
	}

	@Test
	void shouldNotThrowExceptionButLogIfReportingEntryPublishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();

		compositeTestExecutionListener.reportingEntryPublished(testIdentifier, ReportEntry.from("one", "two"));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class,
			"reportingEntryPublished");
	}

	@Test
	void shouldNotThrowExceptionButLogIfTesPlanExecutionStartedListenerMethodFails(
			LogRecordListener logRecordListener) {
		DemoMethodTestDescriptor testDescriptor = getDemoMethodTestDescriptor();

		compositeTestExecutionListener.testPlanExecutionStarted(TestPlan.from(Collections.singleton(testDescriptor)));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class,
			"testPlanExecutionStarted");
	}

	@Test
	void shouldNotThrowExceptionButLogIfTesPlanExecutionFinishedListenerMethodFails(
			LogRecordListener logRecordListener) {
		DemoMethodTestDescriptor testDescriptor = getDemoMethodTestDescriptor();

		compositeTestExecutionListener.testPlanExecutionFinished(TestPlan.from(Collections.singleton(testDescriptor)));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingTestExecutionListener.class,
			"testPlanExecutionFinished");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionJustStartedEagerTestListenerMethodFails(
			LogRecordListener logRecordListener) {
		registry.registerListeners(new ThrowingEagerTestExecutionListener());

		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();
		compositeTestExecutionListener.executionStarted(testIdentifier);

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEagerTestExecutionListener.class,
			"executionJustStarted");
	}

	@Test
	void shouldNotThrowExceptionButLogIfExecutionJustFinishedEagerTestListenerMethodFails(
			LogRecordListener logRecordListener) {
		registry.registerListeners(new ThrowingEagerTestExecutionListener());

		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();
		compositeTestExecutionListener.executionFinished(testIdentifier, mock(TestExecutionResult.class));

		assertThatTestListenerErrorLogged(logRecordListener, ThrowingEagerTestExecutionListener.class,
			"executionJustFinished");
	}

	@Test
	void shouldThrowOutOfMemoryExceptionAndStopListenerWithoutLog(LogRecordListener logRecordListener) {
		TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry();
		registry.registerListeners(new TestExecutionListener() {
			@Override
			public void executionStarted(TestIdentifier testIdentifier) {
				throw new OutOfMemoryError();
			}
		});
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();
		assertThatThrownBy(
			() -> registry.getCompositeTestExecutionListener().executionStarted(testIdentifier)).isInstanceOf(
				OutOfMemoryError.class);

		assertNotLogs(logRecordListener);
	}

	@Test
	void shouldThrowOutOfMemoryExceptionAndStopEagerListenerWithoutLog(LogRecordListener logRecordListener) {
		TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry();
		registry.registerListeners(new TestExecutionListenerRegistry.EagerTestExecutionListener() {
			@Override
			public void executionJustStarted(TestIdentifier testIdentifier) {
				throw new OutOfMemoryError();
			}
		});
		TestIdentifier testIdentifier = getSampleMethodTestIdentifier();
		assertThatThrownBy(
			() -> registry.getCompositeTestExecutionListener().executionStarted(testIdentifier)).isInstanceOf(
				OutOfMemoryError.class);

		assertNotLogs(logRecordListener);
	}

	private LogRecord firstWarnLogRecord(LogRecordListener logRecordListener) throws AssertionError {
		return logRecordListener.stream(TestExecutionListenerRegistry.class, Level.WARNING).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find error log record"));
	}

	private void assertNotLogs(LogRecordListener logRecordListener) throws AssertionError {
		assertThat(logRecordListener.stream(TestExecutionListenerRegistry.class, Level.WARNING).count()).isZero();
	}

	private TestIdentifier getSampleMethodTestIdentifier() {
		DemoMethodTestDescriptor demoMethodTestDescriptor = getDemoMethodTestDescriptor();
		return TestIdentifier.from(demoMethodTestDescriptor);
	}

	private void assertThatTestListenerErrorLogged(LogRecordListener logRecordListener, Class<?> listenerClass,
			String methodName) {
		assertThat(firstWarnLogRecord(logRecordListener).getMessage()).startsWith(
			"TestExecutionListener [" + listenerClass.getName() + "] threw exception for method: " + methodName);
	}

	private DemoMethodTestDescriptor getDemoMethodTestDescriptor() {
		Method method = ReflectionUtils.findMethod(this.getClass(), "getDemoMethodTestDescriptor",
			new Class<?>[0]).orElseThrow();
		return new DemoMethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(), method);
	}

	private static class ThrowingEagerTestExecutionListener extends ThrowingTestExecutionListener
			implements TestExecutionListenerRegistry.EagerTestExecutionListener {
		@Override
		public void executionJustStarted(TestIdentifier testIdentifier) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			throw new RuntimeException("failed to invoke listener");
		}
	}

	private static class ThrowingTestExecutionListener implements TestExecutionListener {
		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void dynamicTestRegistered(TestIdentifier testIdentifier) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			throw new RuntimeException("failed to invoke listener");
		}

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			throw new RuntimeException("failed to invoke listener");
		}
	}

}
