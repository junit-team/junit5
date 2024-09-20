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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;

/**
 * @since 1.3
 */
class SingleLock implements ResourceLock {

	private final Lock lock;

	SingleLock(Lock lock) {
		this.lock = lock;
	}

	// for tests only
	Lock getLock() {
		return this.lock;
	}

	@Override
	public ResourceLock acquire() throws InterruptedException {
		ForkJoinPool.managedBlock(new SingleLockManagedBlocker());
		return this;
	}

	@Override
	public void release() {
		this.lock.unlock();
	}

	private class SingleLockManagedBlocker implements ForkJoinPool.ManagedBlocker {

		private volatile boolean acquired;

		@Override
		public boolean block() throws InterruptedException {
			if (!this.acquired) {
				SingleLock.this.lock.lockInterruptibly();
				this.acquired = true;
			}
			return true;
		}

		@Override
		public boolean isReleasable() {
			return this.acquired || (this.acquired = SingleLock.this.lock.tryLock());
		}

	}

	static class GlobalReadLock extends SingleLock {
		GlobalReadLock(Lock lock) {
			super(lock);
		}

		@Override
		public boolean isCompatible(ResourceLock other) {
			return !(other instanceof GlobalReadWriteLock);
		}
	}

	static class GlobalReadWriteLock extends SingleLock {
		GlobalReadWriteLock(Lock lock) {
			super(lock);
		}
	}
}
