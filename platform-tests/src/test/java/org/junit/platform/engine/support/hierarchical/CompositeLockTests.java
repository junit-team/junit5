/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * @since 1.3
 */
class CompositeLockTests {

	@Test
	@SuppressWarnings("resource")
	void acquiresAllLocksInOrder() throws Exception {
		ReentrantLock lock1 = spy(new ReentrantLock());
		ReentrantLock lock2 = spy(new ReentrantLock());

		new CompositeLock(asList(lock1, lock2)).acquire();

		InOrder inOrder = inOrder(lock1, lock2);
		inOrder.verify(lock1).lockInterruptibly();
		inOrder.verify(lock2).lockInterruptibly();
		assertTrue(lock1.isLocked());
		assertTrue(lock2.isLocked());
	}

	@Test
	@SuppressWarnings("resource")
	void releasesAllLocksInReverseOrder() throws Exception {
		ReentrantLock lock1 = spy(new ReentrantLock());
		ReentrantLock lock2 = spy(new ReentrantLock());

		new CompositeLock(asList(lock1, lock2)).acquire().close();

		InOrder inOrder = inOrder(lock1, lock2);
		inOrder.verify(lock2).unlock();
		inOrder.verify(lock1).unlock();
		assertFalse(lock1.isLocked());
		assertFalse(lock2.isLocked());
	}

	@Test
	@SuppressWarnings("resource")
	void releasesLocksInReverseOrderWhenInterruptedDuringAcquire() throws Exception {
		CountDownLatch firstTwoLocksWereLocked = new CountDownLatch(2);
		Lock firstLock = mockLock("firstLock", firstTwoLocksWereLocked);
		Lock secondLock = mockLock("secondLock", firstTwoLocksWereLocked);
		Lock unavailableLock = spy(new ReentrantLock());
		unavailableLock.lock();

		Thread thread = new Thread(() -> {
			try {
				new CompositeLock(asList(firstLock, secondLock, unavailableLock)).acquire();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		thread.start();
		firstTwoLocksWereLocked.await();
		thread.interrupt();
		thread.join();

		InOrder inOrder = inOrder(firstLock, secondLock);
		inOrder.verify(secondLock).unlock();
		inOrder.verify(firstLock).unlock();
		verify(unavailableLock, never()).unlock();
	}

	private Lock mockLock(String name, CountDownLatch countDownWhenLocked) throws InterruptedException {
		Lock lock = mock(Lock.class, name);
		doAnswer(invocation -> {
			countDownWhenLocked.countDown();
			return null;
		}).when(lock).lockInterruptibly();
		return lock;
	}

}
