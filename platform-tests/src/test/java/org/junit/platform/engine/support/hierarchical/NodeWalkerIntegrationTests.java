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
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.commons.annotation.UseResource;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

class NodeWalkerIntegrationTests {

	@Test
	void pullUpExclusiveChildResourcesToTestClass() {
		NodeExecutor<?> engineNodeExecutor = createRootNodeExecutor(TestCaseWithResourceLock.class);

		assertThat(engineNodeExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testClassExecutor = engineNodeExecutor.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);
		assertThat(testClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.Concurrent);

		assertThat(testClassExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(testMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.SameThread);
	}

	@Test
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource() {
		NodeExecutor<?> engineNodeExecutor = createRootNodeExecutor(TestCaseWithoutResourceLock.class);

		assertThat(engineNodeExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testClassExecutor = engineNodeExecutor.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(testClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.Concurrent);

		assertThat(testClassExecutor.getChildren()).hasSize(2);
		NodeExecutor<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(SingleLock.class);
		assertThat(testMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.Concurrent);

		NodeExecutor<?> nestedTestClassExecutor = testClassExecutor.getChildren().get(1);
		assertThat(nestedTestClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);
		assertThat(nestedTestClassExecutor.getExecutionMode()).isEqualTo(ExecutionMode.Concurrent);

		assertThat(nestedTestClassExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> nestedTestMethodExecutor = nestedTestClassExecutor.getChildren().get(0);
		assertThat(nestedTestMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
		assertThat(nestedTestMethodExecutor.getExecutionMode()).isEqualTo(ExecutionMode.SameThread);
	}

	private NodeExecutor<?> createRootNodeExecutor(Class<?> testClass) {
		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		TestDescriptor testDescriptor = new JupiterTestEngine().discover(discoveryRequest,
			UniqueId.forEngine("junit-jupiter"));
		ExecutionRequest executionRequest = new ExecutionRequest(testDescriptor, null, null);
		HierarchicalTestExecutor<?> executor = new HierarchicalTestExecutor<>(executionRequest, null, null);
		return executor.createRootNodeExecutor();
	}

	@UseResource("a")
	static class TestCaseWithResourceLock {
		@Test
		@UseResource("b")
		void test() {
		}
	}

	static class TestCaseWithoutResourceLock {
		@Test
		@UseResource("a")
		void test() {
		}

		@Nested
		@UseResource("c")
		class NestedTestCaseWithResourceLock {
			@Test
			@UseResource("b")
			void test() {
			}
		}
	}
}
