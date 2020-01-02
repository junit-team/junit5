/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

class TestPlanTests {

	private EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("foo"), "Foo");

	@Test
	void doesNotContainTestsForEmptyContainers() {
		engineDescriptor.addChild(
			new AbstractTestDescriptor(engineDescriptor.getUniqueId().append("test", "bar"), "Bar") {
				@Override
				public Type getType() {
					return Type.CONTAINER;
				}
			});

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

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

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

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

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		assertThat(testPlan.containsTests()).as("contains tests").isTrue();
	}
}
