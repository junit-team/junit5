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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.RunListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for {@link RunListenerAdapter}.
 *
 * @since 1.0
 */
class RunListenerAdapterTests {

	private RunListener listener;
	private RunListenerAdapter adapter;

	@BeforeEach
	public void setUp() {
		listener = mock(RunListener.class);
		adapter = new RunListenerAdapter(listener);
	}

	@Test
	void notifiedWithCorrectNamesWhenMethodExecutionStarted() throws Exception {
		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);

		TestPlan testPlan = TestPlan.from(Collections.singletonList(new EngineDescriptor(newId(), "Luke's Plan")));
		adapter.testPlanExecutionStarted(testPlan);

		TestIdentifier methodIdentifier = newMethodIdentifierOnTestPlan(testPlan);

		adapter.executionStarted(methodIdentifier);
		verify(listener).testStarting(entryCaptor.capture());

		ReportEntry entry = entryCaptor.getValue();
		assertEquals(MY_TEST_METHOD_NAME + "()", entry.getName());
		assertEquals(MyTestClass.class.getName(), entry.getSourceName());
		assertNull(entry.getStackTraceWriter());
	}

	@Test
	void notNotifiedWhenClassExecutionStarted() throws Exception {
		adapter.executionStarted(newClassIdentifier());
		verify(listener, never()).testStarting(any());
	}

	@Test
	void notNotifiedWhenEngineExecutionStarted() throws Exception {
		adapter.executionStarted(newEngineIdentifier());
		verify(listener, never()).testStarting(any());
	}

	@Test
	void notifiedWhenMethodExecutionSkipped() throws Exception {
		adapter.executionSkipped(newMethodIdentifier(), "test");
		verify(listener).testSkipped(any());
	}

	@Test
	void notifiedWithCorrectNamesWhenClassExecutionSkipped() throws Exception {
		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		TestPlan testPlan = TestPlan.from(Collections.singletonList(new EngineDescriptor(newId(), "Luke's Plan")));
		adapter.testPlanExecutionStarted(testPlan);

		TestIdentifier classIdentifier = newClassIdentifierOnTestPlan(testPlan);

		adapter.executionSkipped(classIdentifier, "test");
		verify(listener).testSkipped(entryCaptor.capture());

		ReportEntry entry = entryCaptor.getValue();
		assertTrue(MyTestClass.class.getTypeName().contains(entry.getName()));
		assertEquals("<unrooted>", entry.getSourceName());
	}

	@Test
	void notifiedWhenEngineExecutionSkipped() throws Exception {
		adapter.executionSkipped(newEngineIdentifier(), "test");
		verify(listener).testSkipped(any());
	}

	@Test
	void notifiedWhenMethodExecutionAborted() throws Exception {
		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.aborted(null));
		verify(listener).testAssumptionFailure(any());
	}

	@Test
	void notifiedWhenClassExecutionAborted() throws Exception {
		adapter.executionFinished(newClassIdentifier(), TestExecutionResult.aborted(null));
		verify(listener).testAssumptionFailure(any());
	}

	@Test
	void notifiedWhenMethodExecutionFailed() throws Exception {
		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.failed(new RuntimeException()));
		verify(listener).testFailed(any());
	}

	@Test
	void notifiedWithCorrectNamesWhenClassExecutionFailed() throws Exception {
		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		TestPlan testPlan = TestPlan.from(Collections.singletonList(new EngineDescriptor(newId(), "Luke's Plan")));
		adapter.testPlanExecutionStarted(testPlan);

		adapter.executionFinished(newClassIdentifierOnTestPlan(testPlan),
			TestExecutionResult.failed(new RuntimeException()));
		verify(listener).testFailed(entryCaptor.capture());

		ReportEntry entry = entryCaptor.getValue();
		assertEquals("<unrooted>", entry.getSourceName());
		assertNotNull(entry.getStackTraceWriter());
	}

	@Test
	void notifiedWhenMethodExecutionSucceeded() throws Exception {
		adapter.executionFinished(newMethodIdentifier(), TestExecutionResult.successful());
		verify(listener).testSucceeded(any());
	}

	@Test
	void notNotifiedWhenClassExecutionSucceeded() throws Exception {
		adapter.executionFinished(newClassIdentifier(), TestExecutionResult.successful());
		verify(listener, never()).testSucceeded(any());
	}

	@Test
	void notifiedWithParentDisplayNameWhenTestClassUnknown() throws Exception {
		// Set up a test plan
		TestPlan plan = TestPlan.from(Collections.singletonList(new EngineDescriptor(newId(), "Luke's Plan")));
		adapter.testPlanExecutionStarted(plan);

		// Use the test plan to set up child with parent.
		final String parentDisplay = "I am your father";
		TestIdentifier child = newSourcelessIdentifierWithParent(plan, parentDisplay);
		adapter.executionStarted(child);

		// Check that the adapter has informed Surefire that the test has been invoked,
		// with the parent name as source (since the test case itself had no source).
		ArgumentCaptor<ReportEntry> entryCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		verify(listener).testStarting(entryCaptor.capture());
		assertEquals(parentDisplay, entryCaptor.getValue().getSourceName());
	}

	private static TestIdentifier newMethodIdentifier() throws Exception {
		TestDescriptor testDescriptor = new MethodTestDescriptor(newId(), MyTestClass.class,
			MyTestClass.class.getDeclaredMethod(MY_TEST_METHOD_NAME));
		testDescriptor.setParent(new ClassTestDescriptor(newId(), MyTestClass.class));
		return TestIdentifier.from(testDescriptor);
	}

	private static TestIdentifier newMethodIdentifierOnTestPlan(TestPlan plan) throws Exception {
		TestDescriptor testDescriptor = new MethodTestDescriptor(UniqueId.forEngine("method"), MyTestClass.class,
			MyTestClass.class.getDeclaredMethod(MY_TEST_METHOD_NAME));
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(UniqueId.forEngine("class"),
			MyTestClass.class);
		testDescriptor.setParent(classTestDescriptor);

		TestIdentifier classTestIdentifier = TestIdentifier.from(classTestDescriptor);
		TestIdentifier testMethodIdentifier = TestIdentifier.from(testDescriptor);

		plan.add(classTestIdentifier);
		plan.add(testMethodIdentifier);

		return testMethodIdentifier;
	}

	private static TestIdentifier newClassIdentifier() {
		TestDescriptor testDescriptor = new ClassTestDescriptor(newId(), MyTestClass.class);
		return TestIdentifier.from(testDescriptor);
	}

	private static TestIdentifier newClassIdentifierOnTestPlan(TestPlan plan) {
		TestDescriptor testDescriptor = new ClassTestDescriptor(newId(), MyTestClass.class);
		TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);
		plan.add(testIdentifier);

		return testIdentifier;
	}

	private static TestIdentifier newSourcelessIdentifierWithParent(TestPlan testPlan, String parentDisplay) {
		// A parent test identifier with a name.
		TestDescriptor parent = mock(TestDescriptor.class);
		when(parent.getUniqueId()).thenReturn(newId());
		when(parent.getDisplayName()).thenReturn(parentDisplay);
		when(parent.getLegacyReportingName()).thenReturn(parentDisplay);
		TestIdentifier parentId = TestIdentifier.from(parent);

		// The (child) test case that is to be executed as part of a test plan.
		TestDescriptor child = mock(TestDescriptor.class);
		when(child.getUniqueId()).thenReturn(newId());
		when(child.isTest()).thenReturn(true);

		// Ensure the child source is null yet that there is a parent -- the special case to be tested.
		when(child.getSource()).thenReturn(Optional.empty());
		when(child.getParent()).thenReturn(Optional.of(parent));
		TestIdentifier childId = TestIdentifier.from(child);

		testPlan.add(childId);
		testPlan.add(parentId);

		return childId;
	}

	private static TestIdentifier newEngineIdentifier() {
		TestDescriptor testDescriptor = new EngineDescriptor(newId(), "engine");
		return TestIdentifier.from(testDescriptor);
	}

	private static UniqueId newId() {
		return UniqueId.forEngine("engine");
	}

	private static final String MY_TEST_METHOD_NAME = "myTestMethod";
	private static class MyTestClass {
		@Test
		void myTestMethod() {
		}
	}
}
