/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.junit.platform.surefire.provider;

import static org.mockito.ArgumentMatchers.any;
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
