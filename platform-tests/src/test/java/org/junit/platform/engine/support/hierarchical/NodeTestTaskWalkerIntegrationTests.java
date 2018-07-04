/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.3
 */
class NodeTestTaskWalkerIntegrationTests {

	@Test
	void pullUpExclusiveChildResourcesToTestClass() {
		NodeTestTask<?> engineNodeTestTask = prepareNodeTestTaskTree(TestCaseWithResourceLock.class);

		assertThat(engineNodeTestTask.getChildren()).hasSize(1);
		NodeTestTask<?> testClassExecutor = engineNodeTestTask.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);
		assertThat(testClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);

		assertThat(testClassExecutor.getChildren()).hasSize(1);
		NodeTestTask<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(testMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.SAME_THREAD);
	}

	@Test
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource() {
		NodeTestTask<?> engineNodeTestTask = prepareNodeTestTaskTree(TestCaseWithoutResourceLock.class);

		assertThat(engineNodeTestTask.getChildren()).hasSize(1);
		NodeTestTask<?> testClassExecutor = engineNodeTestTask.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(testClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);

		assertThat(testClassExecutor.getChildren()).hasSize(2);
		NodeTestTask<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(SingleLock.class);
		assertThat(testMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);

		NodeTestTask<?> nestedTestClassExecutor = testClassExecutor.getChildren().get(1);
		assertThat(nestedTestClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);
		assertThat(nestedTestClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);

		assertThat(nestedTestClassExecutor.getChildren()).hasSize(1);
		NodeTestTask<?> nestedTestMethodExecutor = nestedTestClassExecutor.getChildren().get(0);
		assertThat(nestedTestMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(nestedTestMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.SAME_THREAD);
	}

	private NodeTestTask<?> prepareNodeTestTaskTree(Class<?> testClass) {
		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		TestDescriptor testDescriptor = new JupiterTestEngine().discover(discoveryRequest,
			UniqueId.forEngine("junit-jupiter"));
		ExecutionRequest executionRequest = new ExecutionRequest(testDescriptor, null, null);
		HierarchicalTestExecutor<?> executor = new HierarchicalTestExecutor<>(executionRequest, null, null,
			() -> new ThrowableCollector(t -> false));
		return executor.prepareNodeTestTaskTree();
	}

	@ResourceLock("a")
	static class TestCaseWithResourceLock {
		@Test
		@ResourceLock("b")
		void test() {
		}
	}

	static class TestCaseWithoutResourceLock {
		@Test
		@ResourceLock("a")
		void test() {
		}

		@Nested
		@ResourceLock("c")
		class NestedTestCaseWithResourceLock {
			@Test
			@ResourceLock("b")
			void test() {
			}
		}
	}
}
