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

import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.platform.engine.TestDescriptor;

/**
 * @since 1.3
 */
class NodeTreeWalker {

	private final LockManager lockManager = new LockManager();

	NodeExecutionAdvisor walk(TestDescriptor testDescriptor) {
		NodeExecutionAdvisor advisor = new NodeExecutionAdvisor();
		walk(testDescriptor, advisor);
		return advisor;
	}

	private void walk(TestDescriptor testDescriptor, NodeExecutionAdvisor advisor) {
		Set<ExclusiveResource> exclusiveResources = getExclusiveResources(testDescriptor);
		if (exclusiveResources.isEmpty()) {
			testDescriptor.getChildren().forEach(child -> walk(child, advisor));
		}
		else {
			Set<ExclusiveResource> allResources = new HashSet<>(exclusiveResources);
			advisor.forceDescendantExecutionMode(testDescriptor, SAME_THREAD);
			doForChildrenRecursively(testDescriptor, child -> {
				allResources.addAll(getExclusiveResources(child));
				advisor.forceDescendantExecutionMode(child, SAME_THREAD);
			});
			advisor.useResourceLock(testDescriptor, lockManager.getLockForResources(allResources));
		}
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
