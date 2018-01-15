/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.ForkJoinPool;

class ResourceLockManagedBlocker implements ForkJoinPool.ManagedBlocker {
	private final ResourceLock compositeLock;

	private boolean acquired;

	public ResourceLockManagedBlocker(ResourceLock compositeLock) {
		this.compositeLock = compositeLock;
	}

	@Override
	public boolean block() throws InterruptedException {
		compositeLock.acquire();
		acquired = true;
		return true;
	}

	@Override
	public boolean isReleasable() {
		return acquired;
	}
}
