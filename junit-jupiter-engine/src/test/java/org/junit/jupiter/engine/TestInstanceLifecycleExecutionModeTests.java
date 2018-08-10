/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class TestInstanceLifecycleExecutionModeTests extends AbstractJupiterTestEngineTests {

	@Test
	void methodsInTestClassesWithInstancePerClassHaveExecutionModeSameThread() {
		var engineDescriptor = discoverTests(selectClass(SimpleTestCase.class));
		var classDescriptor = (Node<?> & TestDescriptor) getOnlyElement(engineDescriptor.getChildren());
		var testDescriptor = (Node<?> & TestDescriptor) getOnlyElement(classDescriptor.getChildren());

		assertExecutionMode(classDescriptor, CONCURRENT);
		assertExecutionMode(testDescriptor, SAME_THREAD);
	}

	@Test
	void methodsInNestedTestClassesWithInstancePerClassInHierarchyHaveExecutionModeSameThread() {
		var engineDescriptor = discoverTests(selectClass(OuterTestCase.class));
		var outerTestCaseClassDescriptor = firstChild(engineDescriptor, ClassTestDescriptor.class);
		var outerTestMethodDescriptor = firstChild(outerTestCaseClassDescriptor, TestMethodTestDescriptor.class);
		var level1NestedClassDescriptor = firstChild(outerTestCaseClassDescriptor, NestedClassTestDescriptor.class);
		var level1TestMethodDescriptor = firstChild(level1NestedClassDescriptor, TestMethodTestDescriptor.class);
		var level2NestedClassDescriptor = firstChild(level1NestedClassDescriptor, NestedClassTestDescriptor.class);
		var level2TestMethodDescriptor = firstChild(level2NestedClassDescriptor, TestMethodTestDescriptor.class);
		var level3NestedClassDescriptor = firstChild(level2NestedClassDescriptor, NestedClassTestDescriptor.class);
		var level3TestMethodDescriptor = firstChild(level3NestedClassDescriptor, TestMethodTestDescriptor.class);

		assertExecutionMode(outerTestCaseClassDescriptor, CONCURRENT);
		assertExecutionMode(outerTestMethodDescriptor, CONCURRENT);
		assertExecutionMode(level1NestedClassDescriptor, CONCURRENT);
		assertExecutionMode(level1TestMethodDescriptor, CONCURRENT);
		assertExecutionMode(level2NestedClassDescriptor, CONCURRENT);
		assertExecutionMode(level2TestMethodDescriptor, SAME_THREAD);
		assertExecutionMode(level3NestedClassDescriptor, SAME_THREAD);
		assertExecutionMode(level3TestMethodDescriptor, SAME_THREAD);
	}

	private static <T extends Node<?> & TestDescriptor> void assertExecutionMode(T testDescriptor,
			Node.ExecutionMode expectedExecutionMode) {
		assertThat(testDescriptor.getExecutionMode()) //
				.describedAs("ExecutionMode for %s", testDescriptor) //
				.isEqualTo(expectedExecutionMode);
	}

	@SuppressWarnings("unchecked")
	private <T extends Node<?> & TestDescriptor> T firstChild(TestDescriptor engineDescriptor,
			Class<T> testDescriptorClass) {
		return (T) engineDescriptor.getChildren().stream() //
				.filter(testDescriptorClass::isInstance) //
				.findFirst() //
				.orElseGet(() -> fail("No child of type " + testDescriptorClass + " found"));
	}

	@TestInstance(PER_CLASS)
	static class SimpleTestCase {

		@Test
		void test() {
		}

	}

	static class OuterTestCase {
		@Nested
		class LevelOne {
			@Nested
			@TestInstance(PER_CLASS)
			class LevelTwo {
				@Nested
				class LevelThree {
					@Test
					void test() {
					}
				}

				@Test
				void test() {
				}
			}

			@Test
			void test() {
			}
		}

		@Test
		void test() {
		}
	}

}
