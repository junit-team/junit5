/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptorStub;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.mockito.ArgumentCaptor;

class JUnit5FilteringTests {

	@Test
	void appliesFilter() throws Exception {

		TestDescriptor originalParent1 = new TestDescriptorStub("parent1");
		originalParent1.addChild(new TestDescriptorStub("leaf1"));
		TestDescriptor originalParent2 = new TestDescriptorStub("parent2");
		originalParent2.addChild(new TestDescriptorStub("leaf2a"));
		originalParent2.addChild(new TestDescriptorStub("leaf2b"));
		TestPlan fullTestPlan = TestPlan.from(asList(originalParent1, originalParent2));

		TestDescriptor filteredParent = new TestDescriptorStub("parent2");
		filteredParent.addChild(new TestDescriptorStub("leaf2b"));
		TestPlan filteredTestPlan = TestPlan.from(singleton(filteredParent));

		Launcher launcher = mock(Launcher.class);
		ArgumentCaptor<TestDiscoveryRequest> captor = ArgumentCaptor.forClass(TestDiscoveryRequest.class);
		when(launcher.discover(captor.capture())).thenReturn(fullTestPlan).thenReturn(filteredTestPlan);

		JUnit5 runner = new JUnit5(TestClass.class, launcher);
		runner.filter(matchMethodDescription(testDescription("leaf2b")));

		TestDiscoveryRequest lastDiscoveryRequest = captor.getValue();
		List<UniqueIdSelector> uniqueIdSelectors = lastDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
		assertEquals("leaf2b", getOnlyElement(uniqueIdSelectors).getUniqueId());

		Description parentDescription = getOnlyElement(runner.getDescription().getChildren());
		assertEquals(suiteDescription("parent2"), parentDescription);

		Description testDescription = getOnlyElement(parentDescription.getChildren());
		assertEquals(testDescription("leaf2b"), testDescription);
	}

	@Test
	void throwsNoTestsRemainExceptionWhenNoTestIdentifierMatchesFilter() throws Exception {
		TestPlan testPlan = TestPlan.from(singleton(new TestDescriptorStub("test")));

		Launcher launcher = mock(Launcher.class);
		when(launcher.discover(any())).thenReturn(testPlan);

		JUnit5 runner = new JUnit5(TestClass.class, launcher);

		assertThrows(NoTestsRemainException.class,
			() -> runner.filter(matchMethodDescription(suiteDescription("doesNotExist"))));
	}

	private static Description suiteDescription(String uniqueId) {
		return createSuiteDescription(uniqueId, new TestId(uniqueId));
	}

	private static Description testDescription(String uniqueId) {
		return createTestDescription(uniqueId, uniqueId, new TestId(uniqueId));
	}

	private static class TestClass {
	}
}
