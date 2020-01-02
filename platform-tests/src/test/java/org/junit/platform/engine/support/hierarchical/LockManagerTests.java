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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ_WRITE;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.junit.jupiter.api.Test;

/**
 * @since 1.3
 */
class LockManagerTests {

	private LockManager lockManager = new LockManager();

	@Test
	void returnsNopLockWithoutExclusiveResources() {
		Collection<ExclusiveResource> resources = emptySet();

		List<Lock> locks = getLocks(resources, NopLock.class);

		assertThat(locks).isEmpty();
	}

	@Test
	void returnsSingleLockForSingleExclusiveResource() {
		Collection<ExclusiveResource> resources = singleton(new ExclusiveResource("foo", READ));

		List<Lock> locks = getLocks(resources, SingleLock.class);

		assertThat(locks).hasSize(1);
		assertThat(locks.get(0)).isInstanceOf(ReadLock.class);
	}

	@Test
	void returnsCompositeLockForMultipleDifferentExclusiveResources() {
		Collection<ExclusiveResource> resources = asList( //
			new ExclusiveResource("a", READ), //
			new ExclusiveResource("b", READ_WRITE));

		List<Lock> locks = getLocks(resources, CompositeLock.class);

		assertThat(locks).hasSize(2);
		assertThat(locks.get(0)).isInstanceOf(ReadLock.class);
		assertThat(locks.get(1)).isInstanceOf(WriteLock.class);
	}

	@Test
	void reusesSameLockForExclusiveResourceWithSameKey() {
		Collection<ExclusiveResource> resources = singleton(new ExclusiveResource("foo", READ));

		List<Lock> locks1 = getLocks(resources, SingleLock.class);
		List<Lock> locks2 = getLocks(resources, SingleLock.class);

		assertThat(locks1).hasSize(1);
		assertThat(locks2).hasSize(1);
		assertThat(locks1.get(0)).isSameAs(locks2.get(0));
	}

	@Test
	void returnsWriteLockForExclusiveResourceWithBothLockModes() {
		Collection<ExclusiveResource> resources = asList( //
			new ExclusiveResource("bar", READ), //
			new ExclusiveResource("foo", READ), //
			new ExclusiveResource("foo", READ_WRITE), //
			new ExclusiveResource("bar", READ_WRITE));

		List<Lock> locks = getLocks(resources, CompositeLock.class);

		assertThat(locks).hasSize(2);
		assertThat(locks.get(0)).isInstanceOf(WriteLock.class);
		assertThat(locks.get(1)).isInstanceOf(WriteLock.class);
	}

	private List<Lock> getLocks(Collection<ExclusiveResource> resources, Class<? extends ResourceLock> type) {
		ResourceLock lock = lockManager.getLockForResources(resources);
		assertThat(lock).isInstanceOf(type);
		return getLocks(lock);
	}

	private List<Lock> getLocks(ResourceLock resourceLock) {
		if (resourceLock instanceof NopLock) {
			return emptyList();
		}
		if (resourceLock instanceof SingleLock) {
			return singletonList(((SingleLock) resourceLock).getLock());
		}
		return ((CompositeLock) resourceLock).getLocks();
	}
}
