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

import java.util.List;
import java.util.concurrent.locks.Lock;

class CompositeLock {
	private final List<Lock> locks;

	CompositeLock(List<Lock> locks) {
		this.locks = locks;
	}

	void acquire() {
		locks.forEach(Lock::lock);
	}

	void release() {
		for (int i = locks.size() - 1; i >= 0; i--) {
			locks.get(i).unlock();
		}
	}

}
