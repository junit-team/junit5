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

@SuppressWarnings("resource")
class ResourceLockTests {

	@Test
	void nopLocksAreCompatibleWithEverything() {
		var nopLock = NopLock.INSTANCE;

		assertTrue(nopLock.isCompatible(NopLock.INSTANCE));
		assertTrue(nopLock.isCompatible(new SingleLock(anyReentrantLock())));
		assertTrue(nopLock.isCompatible(new SingleLock.GlobalReadLock(anyReentrantLock())));
		assertTrue(nopLock.isCompatible(new SingleLock.GlobalReadWriteLock(anyReentrantLock())));
		assertTrue(nopLock.isCompatible(new CompositeLock(List.of(anyReentrantLock()))));
	}

	@Test
	void singleLocksAreIncompatibleWithNonNopLocks() {
		var singleLock = new SingleLock(anyReentrantLock());

		assertTrue(singleLock.isCompatible(NopLock.INSTANCE));
		assertFalse(singleLock.isCompatible(new SingleLock(anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock.GlobalReadLock(anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new SingleLock.GlobalReadWriteLock(anyReentrantLock())));
		assertFalse(singleLock.isCompatible(new CompositeLock(List.of(anyReentrantLock()))));
	}

	@Test
	void globalReadLockIsCompatibleWithEverythingExceptGlobalReadWriteLock() {
		var globalReadLock = new SingleLock.GlobalReadLock(anyReentrantLock());

		assertTrue(globalReadLock.isCompatible(NopLock.INSTANCE));
		assertTrue(globalReadLock.isCompatible(new SingleLock(anyReentrantLock())));
		assertTrue(globalReadLock.isCompatible(new SingleLock.GlobalReadLock(anyReentrantLock())));
		assertFalse(globalReadLock.isCompatible(new SingleLock.GlobalReadWriteLock(anyReentrantLock())));
		assertTrue(globalReadLock.isCompatible(new CompositeLock(List.of(anyReentrantLock()))));
	}

	@Test
	void globalReadWriteLockIsIncompatibleWithWithNonNopLocks() {
		var globalReadWriteLock = new SingleLock.GlobalReadWriteLock(anyReentrantLock());

		assertTrue(globalReadWriteLock.isCompatible(NopLock.INSTANCE));
		assertFalse(globalReadWriteLock.isCompatible(new SingleLock(anyReentrantLock())));
		assertFalse(globalReadWriteLock.isCompatible(new SingleLock.GlobalReadLock(anyReentrantLock())));
		assertFalse(globalReadWriteLock.isCompatible(new SingleLock.GlobalReadWriteLock(anyReentrantLock())));
		assertFalse(globalReadWriteLock.isCompatible(new CompositeLock(List.of(anyReentrantLock()))));
	}

	@Test
	void compositeLocksAreIncompatibleWithNonNopLocks() {
		CompositeLock compositeLock = new CompositeLock(List.of(anyReentrantLock()));

		assertTrue(compositeLock.isCompatible(NopLock.INSTANCE));
		assertFalse(compositeLock.isCompatible(new SingleLock(anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock.GlobalReadLock(anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock.GlobalReadWriteLock(anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(compositeLock));
	}

	private static ReentrantLock anyReentrantLock() {
		return new ReentrantLock();
	}
}
