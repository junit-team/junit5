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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * @since 1.3
 */
class CompositeLockTests {

	@Test
	@SuppressWarnings("resource")
	void acquiresAllLocksInOrder() throws Exception {
		var lock1 = mock(Lock.class);
		var lock2 = mock(Lock.class);

		new CompositeLock(List.of(lock1, lock2)).acquire();

		var inOrder = inOrder(lock1, lock2);
		inOrder.verify(lock1).lockInterruptibly();
		inOrder.verify(lock2).lockInterruptibly();
	}

	@Test
	@SuppressWarnings("resource")
	void releasesAllLocksInReverseOrder() throws Exception {
		var lock1 = mock(Lock.class);
		var lock2 = mock(Lock.class);

		new CompositeLock(List.of(lock1, lock2)).acquire().close();

		var inOrder = inOrder(lock1, lock2);
		inOrder.verify(lock2).unlock();
		inOrder.verify(lock1).unlock();
	}

	@Test
	@SuppressWarnings("resource")
	void releasesLocksInReverseOrderWhenInterruptedDuringAcquire() throws Exception {
		var firstTwoLocksWereLocked = new CountDownLatch(2);
		var firstLock = mockLock("firstLock", firstTwoLocksWereLocked::countDown);
		var secondLock = mockLock("secondLock", firstTwoLocksWereLocked::countDown);
		var unavailableLock = mockLock("unavailableLock", new CountDownLatch(1)::await);

		var thread = new Thread(() -> {
			try {
				new CompositeLock(List.of(firstLock, secondLock, unavailableLock)).acquire();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		thread.start();
		firstTwoLocksWereLocked.await();
		thread.interrupt();
		thread.join();

		var inOrder = inOrder(firstLock, secondLock);
		inOrder.verify(secondLock).unlock();
		inOrder.verify(firstLock).unlock();
		verify(unavailableLock, never()).unlock();
	}

	private Lock mockLock(String name, Executable lockAction) throws InterruptedException {
		var lock = mock(Lock.class, name);
		doAnswer(invocation -> {
			lockAction.execute();
			return null;
		}).when(lock).lockInterruptibly();
		return lock;
	}

}
