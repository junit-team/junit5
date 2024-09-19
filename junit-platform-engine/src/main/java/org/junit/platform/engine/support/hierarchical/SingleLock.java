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

import static java.util.Collections.unmodifiableNavigableSet;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.singleton;

import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * @since 1.3
 */
class SingleLock implements ResourceLock {

	private final SortedSet<ExclusiveResource> resources;
	private final Lock lock;

	SingleLock(NavigableSet<ExclusiveResource> resources, Lock lock) {
		Preconditions.condition(resources.size() == 1, "SingleLock must have exactly one resource");
		this.resources = unmodifiableNavigableSet(resources);
		this.lock = lock;
	}

	SingleLock(ExclusiveResource resource, Lock lock) {
		this(singleton(resource), lock);
	}

	@Override
	public SortedSet<ExclusiveResource> getResources() {
		return resources;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("resource", getOnlyElement(resources)) //
				.append("lock", lock) //
				.toString();
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
}
