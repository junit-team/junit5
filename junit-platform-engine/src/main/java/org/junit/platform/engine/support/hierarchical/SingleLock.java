/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

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
