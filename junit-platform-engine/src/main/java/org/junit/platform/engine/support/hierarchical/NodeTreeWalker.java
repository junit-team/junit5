/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockScope;

/**
 * @since 1.3
 */
class NodeTreeWalker {

	private static final Set<ExclusiveResource> NO_RESOURCES_INHERITED_AT_ROOT = Collections.emptySet();

	private final LockManager lockManager;
	private final ResourceLock globalReadLock;
	private final ResourceLock globalReadWriteLock;

	NodeTreeWalker() {
		this(new LockManager());
	}

	NodeTreeWalker(LockManager lockManager) {
		this.lockManager = lockManager;
		this.globalReadLock = lockManager.getLockForResource(GLOBAL_READ);
		this.globalReadWriteLock = lockManager.getLockForResource(GLOBAL_READ_WRITE);
	}

	NodeExecutionAdvisor walk(TestDescriptor rootDescriptor) {
		Preconditions.condition(getExclusiveResources(rootDescriptor).isEmpty(),
			"Engine descriptor must not declare exclusive resources");
		NodeExecutionAdvisor advisor = new NodeExecutionAdvisor();
		rootDescriptor.getChildren().forEach(child -> walk(child, child, advisor, NO_RESOURCES_INHERITED_AT_ROOT));
		return advisor;
	}

	private void walk(TestDescriptor globalLockDescriptor, TestDescriptor testDescriptor, NodeExecutionAdvisor advisor,
			Set<ExclusiveResource> inheritedExclusiveResources) {

		Set<ExclusiveResource> exclusiveResources = getExclusiveResources(testDescriptor);
		exclusiveResources.addAll(inheritedExclusiveResources);

		if (hasAnyExclusiveResourceWithSelfScope(exclusiveResources)) {
			assignLocksForDescriptor(globalLockDescriptor, testDescriptor, advisor, exclusiveResources);
		}
		else {
			assignReadLockAndWalkOverChildren(globalLockDescriptor, testDescriptor, advisor, exclusiveResources);
		}
	}

	private static boolean hasAnyExclusiveResourceWithSelfScope(Set<ExclusiveResource> exclusiveResources) {
		return exclusiveResources.stream().anyMatch(resource -> resource.getLockScope() == LockScope.SELF);
	}

	private void assignLocksForDescriptor(TestDescriptor globalLockDescriptor, TestDescriptor testDescriptor,
			NodeExecutionAdvisor advisor, Set<ExclusiveResource> exclusiveResources) {
		Set<ExclusiveResource> allResources = new HashSet<>(exclusiveResources);
		doForChildrenRecursively(testDescriptor, child -> allResources.addAll(getExclusiveResources(child)));

		if (hasReadWriteLockMode(allResources)) {
			forceDescendantExecutionModeRecursively(advisor, testDescriptor);
		}

		if (!globalLockDescriptor.equals(testDescriptor) && allResources.contains(GLOBAL_READ_WRITE)) {
			forceDescendantExecutionModeRecursively(advisor, globalLockDescriptor);
			advisor.useResourceLock(globalLockDescriptor, globalReadWriteLock);
		}

		if (globalLockDescriptor.equals(testDescriptor) && !allResources.contains(GLOBAL_READ_WRITE)) {
			allResources.add(GLOBAL_READ);
		}

		advisor.useResourceLock(testDescriptor, lockManager.getLockForResources(allResources));
	}

	private void assignReadLockAndWalkOverChildren(TestDescriptor globalLockDescriptor, TestDescriptor testDescriptor,
			NodeExecutionAdvisor advisor, Set<ExclusiveResource> exclusiveResources) {
		advisor.useResourceLock(testDescriptor, globalReadLock);
		Set<ExclusiveResource> resourcesToInherit = convertChildrenScopeToSelf(exclusiveResources);
		testDescriptor.getChildren().forEach(child -> walk(globalLockDescriptor, child, advisor, resourcesToInherit));
	}

	private static Set<ExclusiveResource> convertChildrenScopeToSelf(Set<ExclusiveResource> exclusiveResources) {
		return exclusiveResources.stream().map(ExclusiveResource::convertToSelfTarget).collect(Collectors.toSet());
	}

	private boolean hasReadWriteLockMode(Set<ExclusiveResource> allResources) {
		return allResources.stream().anyMatch(resource -> resource.getLockMode() == LockMode.READ_WRITE);
	}

	private void forceDescendantExecutionModeRecursively(NodeExecutionAdvisor advisor, TestDescriptor testDescriptor) {
		advisor.forceDescendantExecutionMode(testDescriptor, SAME_THREAD);
		doForChildrenRecursively(testDescriptor, child -> advisor.forceDescendantExecutionMode(child, SAME_THREAD));
	}

	private Set<ExclusiveResource> getExclusiveResources(TestDescriptor testDescriptor) {
		return NodeUtils.asNode(testDescriptor).getExclusiveResources();
	}

	private void doForChildrenRecursively(TestDescriptor parent, Consumer<TestDescriptor> consumer) {
		parent.getChildren().forEach(child -> {
			consumer.accept(child);
			doForChildrenRecursively(child, consumer);
		});
	}

}
