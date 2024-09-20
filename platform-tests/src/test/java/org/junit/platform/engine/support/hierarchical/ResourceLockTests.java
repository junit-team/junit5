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
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;

class ResourceLockTests {

	@Test
	void nopLocksAreCompatibleWithEverything() {
		var nopLock = NopLock.INSTANCE;

		assertTrue(nopLock.isCompatible(nopLock));
		assertTrue(nopLock.isCompatible(NopLock.INSTANCE));
		assertTrue(nopLock.isCompatible(new SingleLock(anyResource(), anyReentrantLock())));
		assertTrue(nopLock.isCompatible(new CompositeLock(List.of(anyResource()), List.of(anyReentrantLock()))));
	}

	@Test
	void readOnlySingleLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource resourceB = resource("B", LockMode.READ);
		var singleLock = new SingleLock(resourceB, anyReentrantLock());

		assertTrue(singleLock.isCompatible(singleLock));
		assertTrue(singleLock.isCompatible(NopLock.INSTANCE));
		assertTrue(singleLock.isCompatible(new SingleLock(resourceB, anyReentrantLock())));
		assertTrue(singleLock.isCompatible(new SingleLock(resourceB, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resource("B", LockMode.READ_WRITE), anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resource("A"), anyReentrantLock())));
		assertTrue(singleLock.isCompatible(new SingleLock(resource("C"), anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(GLOBAL_READ, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
		assertTrue(singleLock.isCompatible(
			new CompositeLock(List.of(resourceB, resource("C")), List.of(anyReentrantLock(), anyReentrantLock()))));
		assertFalse(singleLock.isCompatible(new CompositeLock(List.of(resource("A1"), resource("A2"), resourceB),
			List.of(anyReentrantLock(), anyReentrantLock(), anyReentrantLock()))));
	}

	@Test
	void readWriteSingleLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		ExclusiveResource resourceB = resource("B", LockMode.READ_WRITE);
		var singleLock = new SingleLock(resourceB, anyReentrantLock());

		assertFalse(singleLock.isCompatible(singleLock));
		assertTrue(singleLock.isCompatible(NopLock.INSTANCE));
		assertFalse(singleLock.isCompatible(new SingleLock(resourceB, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resourceB, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resource("B", LockMode.READ), anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resource("A"), anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(resource("C"), anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(GLOBAL_READ, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
		assertFalse(singleLock.isCompatible(
			new CompositeLock(List.of(resourceB, resource("C")), List.of(anyReentrantLock(), anyReentrantLock()))));
		assertFalse(singleLock.isCompatible(new CompositeLock(List.of(resource("A1"), resource("A2"), resourceB),
			List.of(anyReentrantLock(), anyReentrantLock(), anyReentrantLock()))));
	}

	@Test
	void globalReadLockIsCompatibleWithReadWriteLocksExceptForGlobalReadWriteLock() {
		var globalReadLock = new SingleLock(GLOBAL_READ, anyReentrantLock());

		assertTrue(globalReadLock.isCompatible(globalReadLock));
		assertTrue(globalReadLock.isCompatible(NopLock.INSTANCE));
		assertTrue(globalReadLock.isCompatible(new SingleLock(anyResource(LockMode.READ), anyReentrantLock())));
		assertTrue(globalReadLock.isCompatible(new SingleLock(anyResource(LockMode.READ_WRITE), anyReentrantLock())));
		assertFalse(globalReadLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
	}

	@Test
	void readOnlyCompositeLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		CompositeLock compositeLock = new CompositeLock(List.of(anyResource(LockMode.READ)),
			List.of(anyReentrantLock()));

		assertTrue(compositeLock.isCompatible(compositeLock));
		assertTrue(compositeLock.isCompatible(NopLock.INSTANCE));
		assertTrue(compositeLock.isCompatible(new SingleLock(anyResource(), anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ, anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(
			new CompositeLock(List.of(anyResource(LockMode.READ_WRITE)), List.of(anyReentrantLock()))));
	}

	@Test
	void readWriteCompositeLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		CompositeLock compositeLock = new CompositeLock(List.of(anyResource(LockMode.READ_WRITE)),
			List.of(anyReentrantLock()));

		assertFalse(compositeLock.isCompatible(compositeLock));
		assertTrue(compositeLock.isCompatible(NopLock.INSTANCE));
		assertFalse(compositeLock.isCompatible(new SingleLock(anyResource(), anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ, anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(
			new CompositeLock(List.of(anyResource(LockMode.READ)), List.of(anyReentrantLock()))));
	}

	private static ExclusiveResource anyResource() {
		return anyResource(LockMode.READ);
	}

	private static ExclusiveResource anyResource(LockMode lockMode) {
		return resource("key", lockMode);
	}

	private static ExclusiveResource resource(String key) {
		return resource(key, LockMode.READ);
	}

	private static ExclusiveResource resource(String key, LockMode lockMode) {
		return new ExclusiveResource(key, lockMode);
	}

	private static ReentrantLock anyReentrantLock() {
		return new ReentrantLock();
	}
}