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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.jfr.ExclusiveResourceEvent;

public class JfrReportingLock implements Lock {

	private final Lock delegate;
	private final String key;
	private final ExclusiveResourceEvent.Mode mode;
	private final String owner;

	public JfrReportingLock(Lock delegate, String key, ExclusiveResource.LockMode lockMode, UniqueId owner) {
		this.delegate = delegate;
		this.key = key;
		this.mode = lockMode == ExclusiveResource.LockMode.READ ? ExclusiveResourceEvent.Mode.READ
				: ExclusiveResourceEvent.Mode.READ_WRITE;
		this.owner = owner.toString();
	}

	private void reportAcquiring() {
		ExclusiveResourceEvent.ExclusiveResourceAcquiringEvent event = new ExclusiveResourceEvent.ExclusiveResourceAcquiringEvent();
		event.initialize(key, mode, owner);
		event.commit();
	}

	private void reportAcquired() {
		ExclusiveResourceEvent.ExclusiveResourceAcquiredEvent event = new ExclusiveResourceEvent.ExclusiveResourceAcquiredEvent();
		event.initialize(key, mode, owner);
		event.commit();
	}

	private void reportReleased() {
		ExclusiveResourceEvent.ExclusiveResourceReleasedEvent event = new ExclusiveResourceEvent.ExclusiveResourceReleasedEvent();
		event.initialize(key, mode, owner);
		event.commit();
	}

	@Override
	public void lock() {
		reportAcquiring();
		delegate.lock();
		reportAcquired();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		reportAcquiring();
		delegate.lockInterruptibly();
		reportAcquired();
	}

	@Override
	public boolean tryLock() {
		boolean result = delegate.tryLock();
		if (result) {
			reportAcquired();
		}
		return result;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		boolean result = delegate.tryLock(time, unit);
		if (result) {
			reportAcquired();
		}
		return result;
	}

	@Override
	public void unlock() {
		delegate.unlock();
		reportReleased();
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException("newCondition");
	}
}
