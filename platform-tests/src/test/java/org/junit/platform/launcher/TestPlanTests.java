/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.fakes.TestDescriptorStub;

class TestPlanTests {

	private final ConfigurationParameters configParams = mock();

	private final EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("foo"), "Foo");

	@Test
	void doesNotContainTestsForEmptyContainers() {
		engineDescriptor.addChild(
			new AbstractTestDescriptor(engineDescriptor.getUniqueId().append("test", "bar"), "Bar") {
				@Override
				public Type getType() {
					return Type.CONTAINER;
				}
			});

		var testPlan = TestPlan.from(Set.of(engineDescriptor), configParams);

		assertThat(testPlan.containsTests()).as("contains tests").isFalse();
	}

	@Test
	void containsTestsForTests() {
		engineDescriptor.addChild(
			new AbstractTestDescriptor(engineDescriptor.getUniqueId().append("test", "bar"), "Bar") {
				@Override
				public Type getType() {
					return Type.TEST;
				}
			});

		var testPlan = TestPlan.from(Set.of(engineDescriptor), configParams);

		assertThat(testPlan.containsTests()).as("contains tests").isTrue();
	}

	@Test
	void containsTestsForContainersThatMayRegisterTests() {
		engineDescriptor.addChild(
			new AbstractTestDescriptor(engineDescriptor.getUniqueId().append("test", "bar"), "Bar") {
				@Override
				public Type getType() {
					return Type.CONTAINER;
				}

				@Override
				public boolean mayRegisterTests() {
					return true;
				}
			});

		var testPlan = TestPlan.from(Set.of(engineDescriptor), configParams);

		assertThat(testPlan.containsTests()).as("contains tests").isTrue();
	}

	@Test
	void iteratesInDepthFirstOrder() {
		var container = new TestDescriptorStub(engineDescriptor.getUniqueId().append("container", "bar"), "Bar");
		var test1 = new TestDescriptorStub(container.getUniqueId().append("test", "bar"), "Bar");
		container.addChild(test1);
		engineDescriptor.addChild(container);

		var engineDescriptor2 = new EngineDescriptor(UniqueId.forEngine("baz"), "Baz");
		var test2 = new TestDescriptorStub(engineDescriptor2.getUniqueId().append("test", "baz1"), "Baz");
		var test3 = new TestDescriptorStub(engineDescriptor2.getUniqueId().append("test", "baz2"), "Baz");
		engineDescriptor2.addChild(test2);
		engineDescriptor2.addChild(test3);

		var testPlan = TestPlan.from(List.of(engineDescriptor, engineDescriptor2), configParams);

		var testIdentifiers = StreamSupport.stream(testPlan.spliterator(), false).toList();
		assertThat(testIdentifiers).extracting(TestIdentifier::getUniqueIdObject) //
				.containsExactly(engineDescriptor.getUniqueId(), container.getUniqueId(), test1.getUniqueId(),
					engineDescriptor2.getUniqueId(), test2.getUniqueId(), test3.getUniqueId());
	}

}
