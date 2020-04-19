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
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.DemoMethodTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
// TODO Test other adapter methods.
class ExecutionListenerAdapterTests {

	@Test
	void testReportingEntryPublished() {
		TestDescriptor testDescriptor = getSampleMethodTestDescriptor();

		LauncherDiscoveryResult discoveryResult = new LauncherDiscoveryResult(
			Map.of(mock(TestEngine.class), testDescriptor), null);
		InternalTestPlan testPlan = InternalTestPlan.from(discoveryResult);
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

	private TestDescriptor getSampleMethodTestDescriptor() {
		Method localMethodNamedNothing = ReflectionUtils.findMethod(this.getClass(), "nothing", new Class<?>[0]).get();
		return new DemoMethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(),
			localMethodNamedNothing);
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
