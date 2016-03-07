/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.commons.util.*;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.*;
import org.junit.gen5.engine.reporting.*;
import org.junit.gen5.launcher.*;

//TODO: test other adapter methods
class ExecutionListenerAdapterTests {

	@Test
	void testReportingEntryPublished() {
		MethodTestDescriptor testDescriptor = getSampleMethodTestDescriptor();

		//cannot mock final classes with mockito
		TestPlan testPlan = TestPlan.from(Collections.singleton(testDescriptor));
		TestIdentifier testIdentifier = testPlan.getTestIdentifier(new TestId(testDescriptor.getUniqueId()));

		//not yet spyable with mockito? -> https://github.com/mockito/mockito/issues/146
		MockTestExecutionListener testExecutionListener = new MockTestExecutionListener();
		ExecutionListenerAdapter executionListenerAdapter = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);

		ReportEntry entry = ReportEntry.from("one", "two");
		executionListenerAdapter.reportingEntryPublished(testDescriptor, entry);

		assertThat(testExecutionListener.entry).isEqualTo(entry);
		assertThat(testExecutionListener.testIdentifier).isEqualTo(testIdentifier);
	}

	private MethodTestDescriptor getSampleMethodTestDescriptor() {
		Method localMethodNamedNothing = ReflectionUtils.findMethod(this.getClass(), "nothing", new Class[] {}).get();
		return new MethodTestDescriptor(UniqueId.root("method", "unique_id"), this.getClass(), localMethodNamedNothing);
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
