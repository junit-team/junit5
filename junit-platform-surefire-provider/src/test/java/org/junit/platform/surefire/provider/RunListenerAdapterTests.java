/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.maven.surefire.report.RunListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Unit tests for {@link RunListenerAdapter}.
 *
 * @since 1.0
 */
class RunListenerAdapterTests {

	@Test
	void notifiedWhenMethodExecutionStarted() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionStarted(newMethodIdentifier());

		verify(listener).testStarting(any());
	}

	@Test
	void notNotifiedWhenClassExecutionStarted() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionStarted(newClassIdentifier());

		verify(listener, never()).testStarting(any());
	}

	@Test
	void notNotifiedWhenEngineExecutionStarted() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionStarted(newEngineIdentifier());

		verify(listener, never()).testStarting(any());
	}

	@Test
	void notifiedWhenMethodExecutionSkipped() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionSkipped(newMethodIdentifier(), "test");

		verify(listener).testSkipped(any());
	}

	@Test
	void notifiedWhenClassExecutionSkipped() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionSkipped(newClassIdentifier(), "test");

		verify(listener).testSkipped(any());
	}

	@Test
	void notifiedWhenEngineExecutionSkipped() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionSkipped(newEngineIdentifier(), "test");

		verify(listener).testSkipped(any());
	}

	@Test
	void notifiedWhenMethodExecutionAborted() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.aborted(null));

		verify(listener).testAssumptionFailure(any());
	}

	@Test
	void notifiedWhenClassExecutionAborted() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newClassIdentifier(), TestExecutionResult.aborted(null));

		verify(listener).testAssumptionFailure(any());
	}

	@Test
	void notifiedWhenMethodExecutionFailed() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.failed(new RuntimeException()));

		verify(listener).testFailed(any());
	}

	@Test
	void notifiedWhenClassExecutionFailed() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newClassIdentifier(), TestExecutionResult.failed(new RuntimeException()));

		verify(listener).testFailed(any());
	}

	@Test
	void notifiedWhenMethodExecutionSucceeded() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.successful());

		verify(listener).testSucceeded(any());
	}

	@Test
	void notNotifiedWhenClassExecutionSucceeded() throws Exception {
		RunListener listener = mock(RunListener.class);
		RunListenerAdapter adapter = new RunListenerAdapter(listener);

		adapter.executionFinished(newClassIdentifier(), TestExecutionResult.successful());

		verify(listener, never()).testSucceeded(any());
	}

	private static TestIdentifier newMethodIdentifier() throws Exception {
		TestDescriptor testDescriptor = new MethodTestDescriptor(newId(), TestClass.class,
			TestClass.class.getDeclaredMethod("test1"));
		return TestIdentifier.from(testDescriptor);
	}

	private static TestIdentifier newClassIdentifier() {
		TestDescriptor testDescriptor = new ClassTestDescriptor(newId(), TestClass.class);
		return TestIdentifier.from(testDescriptor);
	}

	private static TestIdentifier newEngineIdentifier() {
		TestDescriptor testDescriptor = new EngineDescriptor(newId(), "engine");
		return TestIdentifier.from(testDescriptor);
	}

	private static UniqueId newId() {
		return UniqueId.forEngine("engine");
	}

	private static class TestClass {

		@Test
		void test1() {
		}
	}
}
