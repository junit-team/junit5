/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;

/**
 * @since 1.3
 */
class CompositeLock implements ResourceLock {

	private final List<Lock> locks;

	CompositeLock(List<Lock> locks) {
		this.locks = locks;
	}

	// for tests only
	List<Lock> getLocks() {
		return locks;
	}

	@Override
	public ResourceLock acquire() throws InterruptedException {
		ForkJoinPool.managedBlock(new CompositeLockManagedBlocker());
		return this;
	}

	private void acquireAllLocks() throws InterruptedException {
		List<Lock> acquiredLocks = new ArrayList<>(locks.size());
		try {
			for (Lock lock : locks) {
				lock.lockInterruptibly();
				acquiredLocks.add(lock);
			}
		}
		catch (InterruptedException e) {
			release(acquiredLocks);
			throw e;
		}
	}

	@Override
	public void release() {
		release(locks);
	}

	private void release(List<Lock> acquiredLocks) {
		for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
			acquiredLocks.get(i).unlock();
		}
	}

	private class CompositeLockManagedBlocker implements ForkJoinPool.ManagedBlocker {

		private boolean acquired;

		@Override
		public boolean block() throws InterruptedException {
			acquireAllLocks();
			acquired = true;
			return true;
		}

		@Override
		public boolean isReleasable() {
			return acquired;
		}

	}

}
