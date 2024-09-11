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

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

class ResourceLockTests {

	private final ReentrantLock reentrantLock = new ReentrantLock();

	@Test
	void singleResourceLocksThatBothHaveTheGlobalReadLockFlagAreCompatible() {
		SingleLock lock1 = new SingleLock(reentrantLock);
		SingleLock lock2 = new SingleLock(reentrantLock);
		assertSymmetry(lock1, lock2);
	}

	@Test
	void singleResourceLocksThatNotBothHaveTheGlobalReadLockFlagAreIncompatible() {
		SingleLock lock1 = new SingleLock(reentrantLock);
		SingleLock lock2 = new SingleLock(reentrantLock);
		SingleLock lock3 = new SingleLock(reentrantLock);
		assertSymmetryNotCompatible(lock1, lock2);
		assertSymmetryNotCompatible(lock1, lock3);
		assertSymmetryNotCompatible(lock2, lock3);
	}

	@Test
	void nopLocksAreCompatibleWithEverything() {
		ResourceLock nop = NopLock.INSTANCE;
		SingleLock singleLockGR = new SingleLock(reentrantLock);
		SingleLock singleLock = new SingleLock(reentrantLock);
		CompositeLock compositeLock = new CompositeLock(List.of(reentrantLock));
		assertSymmetry(nop, singleLockGR);
		assertSymmetry(nop, singleLock);
		assertSymmetry(nop, compositeLock);
	}

	@Test
	void compositeLocksAreIncompatibleWithNonNopLocks() {
		CompositeLock compositeLock = new CompositeLock(List.of(reentrantLock));
		SingleLock singleLockGR = new SingleLock(reentrantLock);
		SingleLock singleLock = new SingleLock(reentrantLock);
		assertSymmetryNotCompatible(compositeLock, singleLockGR);
		assertSymmetryNotCompatible(compositeLock, singleLock);
		assertSymmetryNotCompatible(compositeLock, compositeLock);
	}

	void assertSymmetry(ResourceLock a, ResourceLock b) {
		assertTrue(a.isCompatible(b));
		assertTrue(b.isCompatible(a));
	}

	void assertSymmetryNotCompatible(ResourceLock a, ResourceLock b) {
		assertFalse(a.isCompatible(b));
		assertFalse(b.isCompatible(a));
	}
}
