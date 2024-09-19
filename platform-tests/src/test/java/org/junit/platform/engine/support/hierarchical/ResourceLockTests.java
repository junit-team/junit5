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
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.singleton;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
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
		assertTrue(nopLock.isCompatible(new CompositeLock(singleton(anyResource()), List.of(anyReentrantLock()))));
	}

	@Test
	void singleLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
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
			new CompositeLock(allOf(resourceB, resource("C")), List.of(anyReentrantLock(), anyReentrantLock()))));
		assertFalse(singleLock.isCompatible(new CompositeLock(allOf(resource("A1"), resource("A2"), resourceB),
			List.of(anyReentrantLock(), anyReentrantLock(), anyReentrantLock()))));
	}

	@Test
	void compositeLocksAreIncompatibleWithOtherLocksThatCanPotentiallyCauseDeadlocks() {
		CompositeLock compositeLock = new CompositeLock(singleton(anyResource()), List.of(anyReentrantLock()));

		assertTrue(compositeLock.isCompatible(compositeLock));
		assertTrue(compositeLock.isCompatible(NopLock.INSTANCE));
		assertTrue(compositeLock.isCompatible(new SingleLock(anyResource(), anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ, anyReentrantLock())));
		assertFalse(compositeLock.isCompatible(new SingleLock(GLOBAL_READ_WRITE, anyReentrantLock())));
		assertTrue(compositeLock.isCompatible(compositeLock));
	}

	private static ExclusiveResource anyResource() {
		return resource("key", LockMode.READ);
	}

	private static ExclusiveResource resource(String key) {
		return resource(key, LockMode.READ);
	}

	private static ExclusiveResource resource(String key, LockMode lockMode) {
		return new ExclusiveResource(key, lockMode);
	}

	private static NavigableSet<ExclusiveResource> allOf(ExclusiveResource... resources) {
		var result = new TreeSet<>(ExclusiveResource.COMPARATOR);
		result.addAll(List.of(resources));
		return result;
	}

	private static ReentrantLock anyReentrantLock() {
		return new ReentrantLock();
	}
}
