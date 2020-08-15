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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.3
 */
class NodeTreeWalkerIntegrationTests {

	LockManager lockManager = new LockManager();
	NodeTreeWalker nodeTreeWalker = new NodeTreeWalker(lockManager);

	@Test
	void pullUpExclusiveChildResourcesToTestClass() {
		TestDescriptor engineDescriptor = discover(TestCaseWithResourceLock.class);

		NodeExecutionAdvisor advisor = nodeTreeWalker.walk(engineDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getReadWriteLock("a"), getReadWriteLock("b")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(emptyList());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	@Test
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource() {
		TestDescriptor engineDescriptor = discover(TestCaseWithoutResourceLock.class);

		NodeExecutionAdvisor advisor = nodeTreeWalker.walk(engineDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ)));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		assertThat(testClassDescriptor.getChildren()).hasSize(2);
		Iterator<? extends TestDescriptor> children = testClassDescriptor.getChildren().iterator();
		TestDescriptor testMethodDescriptor = children.next();
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getReadWriteLock("a")));
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).isEmpty();

		TestDescriptor nestedTestClassDescriptor = children.next();
		assertThat(advisor.getResourceLock(nestedTestClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getReadWriteLock("b"), getReadWriteLock("c")));
		assertThat(advisor.getForcedExecutionMode(nestedTestClassDescriptor)).isEmpty();

		TestDescriptor nestedTestMethodDescriptor = getOnlyElement(nestedTestClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(nestedTestMethodDescriptor)).extracting(allLocks()).isEqualTo(emptyList());
		assertThat(advisor.getForcedExecutionMode(nestedTestMethodDescriptor)).contains(SAME_THREAD);
	}

	@Test
	void coarsensGlobalLockToEngineDescriptorChild() {
		TestDescriptor engineDescriptor = discover(TestCaseWithGlobalLockRequiringChild.class);

		NodeExecutionAdvisor advisor = nodeTreeWalker.walk(engineDescriptor);

		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ_WRITE)));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		TestDescriptor nestedTestClassDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(nestedTestClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ)));
		assertThat(advisor.getForcedExecutionMode(nestedTestClassDescriptor)).contains(SAME_THREAD);

		TestDescriptor testMethodDescriptor = getOnlyElement(nestedTestClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ_WRITE)));
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	private static Function<org.junit.platform.engine.support.hierarchical.ResourceLock, List<Lock>> allLocks() {
		return ResourceLockSupport::getLocks;
	}

	private Lock getReadWriteLock(String key) {
		return getLock(new ExclusiveResource(key, READ_WRITE));
	}

	private Lock getLock(ExclusiveResource exclusiveResource) {
		return getOnlyElement(ResourceLockSupport.getLocks(lockManager.getLockForResource(exclusiveResource)));
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

	static class TestCaseWithGlobalLockRequiringChild {
		@Nested
		class NestedTestCaseWithResourceLock {
			@Test
			@ResourceLock(ExclusiveResource.GLOBAL_KEY)
			void test() {
			}
		}
	}
}
