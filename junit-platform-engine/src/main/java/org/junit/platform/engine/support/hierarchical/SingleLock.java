package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;

public class SingleLock implements ResourceLock {
	private final Lock lock;

	public SingleLock(Lock lock) {
		this.lock = lock;
	}

	@Override
	public ResourceLock acquire() throws InterruptedException {
		ForkJoinPool.managedBlock(new SingleLockManagedBlocker());
		return this;
	}

	@Override
	public void release() {
		lock.unlock();
	}

	private class SingleLockManagedBlocker implements ForkJoinPool.ManagedBlocker {
		private boolean acquired;

		@Override
		public boolean block() throws InterruptedException {
			lock.lockInterruptibly();
			acquired = true;
			return true;
		}

		@Override
		public boolean isReleasable() {
			return acquired || lock.tryLock();
		}
	}
}
