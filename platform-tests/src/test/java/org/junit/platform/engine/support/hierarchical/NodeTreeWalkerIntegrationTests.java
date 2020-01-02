/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.Iterator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.3
 */
class NodeTreeWalkerIntegrationTests {

	@Test
	void pullUpExclusiveChildResourcesToTestClass() {
		TestDescriptor engineDescriptor = discover(TestCaseWithResourceLock.class);
		NodeExecutionAdvisor advisor = new NodeTreeWalker().walk(engineDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).isInstanceOf(CompositeLock.class);
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).isInstanceOf(NopLock.class);
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(ExecutionMode.SAME_THREAD);
	}

	@Test
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource() {
		TestDescriptor engineDescriptor = discover(TestCaseWithoutResourceLock.class);
		NodeExecutionAdvisor advisor = new NodeTreeWalker().walk(engineDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).isInstanceOf(NopLock.class);
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		assertThat(testClassDescriptor.getChildren()).hasSize(2);
		Iterator<? extends TestDescriptor> children = testClassDescriptor.getChildren().iterator();
		TestDescriptor testMethodDescriptor = children.next();
		assertThat(advisor.getResourceLock(testMethodDescriptor)).isInstanceOf(SingleLock.class);
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).isEmpty();

		TestDescriptor nestedTestClassDescriptor = children.next();
		assertThat(advisor.getResourceLock(nestedTestClassDescriptor)).isInstanceOf(CompositeLock.class);
		assertThat(advisor.getForcedExecutionMode(nestedTestClassDescriptor)).isEmpty();

		TestDescriptor nestedTestMethodDescriptor = getOnlyElement(nestedTestClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(nestedTestMethodDescriptor)).isInstanceOf(NopLock.class);
		assertThat(advisor.getForcedExecutionMode(nestedTestMethodDescriptor)).contains(ExecutionMode.SAME_THREAD);
	}

	private TestDescriptor discover(Class<?> testClass) {
		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		return new JupiterTestEngine().discover(discoveryRequest, UniqueId.forEngine("junit-jupiter"));
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
