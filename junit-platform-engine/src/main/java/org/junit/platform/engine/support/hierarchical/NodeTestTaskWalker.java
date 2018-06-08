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

import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @since 1.3
 */
class NodeTestTaskWalker {

	private final LockManager lockManager = new LockManager();

	void walk(NodeTestTask<?> nodeTestTask) {
		if (nodeTestTask.getExclusiveResources().isEmpty()) {
			nodeTestTask.getChildren().forEach(this::walk);
		}
		else {
			Set<ExclusiveResource> allResources = new HashSet<>(nodeTestTask.getExclusiveResources());
			doForChildrenRecursively(nodeTestTask, child -> {
				allResources.addAll(child.getExclusiveResources());
				child.setForcedExecutionMode(SAME_THREAD);
			});
			nodeTestTask.setResourceLock(lockManager.getLockForResources(allResources));
		}
	}

	private void doForChildrenRecursively(NodeTestTask<?> parent, Consumer<NodeTestTask<?>> consumer) {
		parent.getChildren().forEach(child -> {
			consumer.accept(child);
			doForChildrenRecursively(child, consumer);
		});
	}

}
