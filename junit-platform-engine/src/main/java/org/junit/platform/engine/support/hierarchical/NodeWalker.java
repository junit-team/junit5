package org.junit.platform.engine.support.hierarchical;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

class NodeWalker {

	private final LockManager lockManager;

	NodeWalker(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public <C extends EngineExecutionContext> void walk(NodeExecutor<C> nodeExecutor) {
		if (nodeExecutor.getNode().getExclusiveResources().isEmpty()) {
			nodeExecutor.getChildren().forEach(this::walk);
		} else {
			Set<ExclusiveResource> allResources = new HashSet<>(nodeExecutor.getNode().getExclusiveResources());
			doForChildrenRecursively(nodeExecutor, child -> allResources.addAll(child.getNode().getExclusiveResources()));
			nodeExecutor.setResourceLock(lockManager.getLockForResources(allResources));
		}
	}

	private <C extends EngineExecutionContext> void doForChildrenRecursively(NodeExecutor<C> parent, Consumer<NodeExecutor<C>> consumer) {
		parent.getChildren().forEach(child -> {
			consumer.accept(child);
			doForChildrenRecursively(child, consumer);
		});
	}


}
