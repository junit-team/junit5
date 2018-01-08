package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.ForkJoinPool;

class ResourceLockManagedBlocker implements ForkJoinPool.ManagedBlocker {
	private final ResourceLock compositeLock;

	private boolean acquired;

	public ResourceLockManagedBlocker(ResourceLock compositeLock) {
		this.compositeLock = compositeLock;
	}

	@Override
	public boolean block() throws InterruptedException {
		compositeLock.acquire();
		acquired = true;
		return true;
	}

	@Override
	public boolean isReleasable() {
		return acquired;
	}
}
