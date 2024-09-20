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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;

class ResourceLockTests {

	@Test
	void nopLocksAreCompatibleWithEverything() {
		assertCompatible(nopLock(), nopLock());
		assertCompatible(nopLock(), singleLock(anyReadOnlyResource()));
		assertCompatible(nopLock(), compositeLock(anyReadOnlyResource()));
	}

	@Test
	void readOnlySingleLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource bR = readOnlyResource("b");

		assertCompatible(singleLock(bR), nopLock());
		assertCompatible(singleLock(bR), singleLock(bR));
		assertIncompatible(singleLock(bR), singleLock(readWriteResource("b")));
		assertIncompatible(singleLock(bR), singleLock(readOnlyResource("a")));
		assertCompatible(singleLock(bR), singleLock(readOnlyResource("c")));
		assertIncompatible(singleLock(bR), singleLock(GLOBAL_READ));
		assertIncompatible(singleLock(bR), singleLock(GLOBAL_READ_WRITE));
		assertCompatible(singleLock(bR), compositeLock(bR, readOnlyResource("c")));
		assertIncompatible(singleLock(bR), compositeLock(readOnlyResource("a1"), readOnlyResource("a2"), bR));
	}

	@Test
	void readWriteSingleLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource bRW = readWriteResource("b");

		assertCompatible(singleLock(bRW), nopLock());
		assertIncompatible(singleLock(bRW), singleLock(bRW));
		assertIncompatible(singleLock(bRW), compositeLock(bRW));
		assertIncompatible(singleLock(bRW), singleLock(readOnlyResource("a")));
		assertIncompatible(singleLock(bRW), singleLock(readOnlyResource("b")));
		assertIncompatible(singleLock(bRW), singleLock(readOnlyResource("c")));
		assertIncompatible(singleLock(bRW), singleLock(GLOBAL_READ));
		assertIncompatible(singleLock(bRW), singleLock(GLOBAL_READ_WRITE));
		assertIncompatible(singleLock(bRW), compositeLock(bRW, readOnlyResource("c")));
		assertIncompatible(singleLock(bRW), compositeLock(readOnlyResource("a1"), readOnlyResource("a2"), bRW));
	}

	@Test
	void globalReadLockIsCompatibleWithReadWriteLocksExceptForGlobalReadWriteLock() {
		assertCompatible(singleLock(GLOBAL_READ), nopLock());
		assertCompatible(singleLock(GLOBAL_READ), singleLock(GLOBAL_READ));
		assertCompatible(singleLock(GLOBAL_READ), singleLock(anyReadOnlyResource()));
		assertCompatible(singleLock(GLOBAL_READ), singleLock(anyReadWriteResource()));
		assertIncompatible(singleLock(GLOBAL_READ), singleLock(GLOBAL_READ_WRITE));
	}

	@Test
	void readOnlyCompositeLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource bR = readOnlyResource("b");

		assertCompatible(compositeLock(bR), nopLock());
		assertCompatible(compositeLock(bR), singleLock(bR));
		assertCompatible(compositeLock(bR), compositeLock(bR));
		assertIncompatible(compositeLock(bR), singleLock(GLOBAL_READ));
		assertIncompatible(compositeLock(bR), singleLock(GLOBAL_READ_WRITE));
		assertIncompatible(compositeLock(bR), compositeLock(readOnlyResource("a")));
		assertCompatible(compositeLock(bR), compositeLock(readOnlyResource("c")));
		assertIncompatible(compositeLock(bR), compositeLock(readWriteResource("b")));
		assertIncompatible(compositeLock(bR), compositeLock(bR, readWriteResource("b")));
	}

	@Test
	void readWriteCompositeLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource bRW = readWriteResource("b");

		assertCompatible(compositeLock(bRW), nopLock());
		assertIncompatible(compositeLock(bRW), singleLock(bRW));
		assertIncompatible(compositeLock(bRW), compositeLock(bRW));
		assertIncompatible(compositeLock(bRW), singleLock(readOnlyResource("a")));
		assertIncompatible(compositeLock(bRW), singleLock(readOnlyResource("b")));
		assertIncompatible(compositeLock(bRW), singleLock(readOnlyResource("c")));
		assertIncompatible(compositeLock(bRW), singleLock(GLOBAL_READ));
		assertIncompatible(compositeLock(bRW), singleLock(GLOBAL_READ_WRITE));
		assertIncompatible(compositeLock(bRW), compositeLock(readOnlyResource("a")));
		assertIncompatible(compositeLock(bRW), compositeLock(readOnlyResource("b"), readOnlyResource("c")));
	}

	private static void assertCompatible(ResourceLock first, ResourceLock second) {
		assertTrue(first.isCompatible(second));
	}

	private static void assertIncompatible(ResourceLock first, ResourceLock second) {
		assertFalse(first.isCompatible(second));
	}

	private static ResourceLock nopLock() {
		return NopLock.INSTANCE;
	}

	private static SingleLock singleLock(ExclusiveResource resource) {
		return new SingleLock(resource, anyLock());
	}

	private static CompositeLock compositeLock(ExclusiveResource... resources) {
		return new CompositeLock(List.of(resources),
			IntStream.range(0, resources.length).mapToObj(__ -> anyLock()).toList());
	}

	private static ExclusiveResource anyReadOnlyResource() {
		return readOnlyResource("key");
	}

	private static ExclusiveResource anyReadWriteResource() {
		return readWriteResource("key");
	}

	private static ExclusiveResource readOnlyResource(String key) {
		return new ExclusiveResource(key, LockMode.READ);
	}

	private static ExclusiveResource readWriteResource(String key) {
		return new ExclusiveResource(key, LockMode.READ_WRITE);
	}

	private static Lock anyLock() {
		return new ReentrantLock();
	}
}
