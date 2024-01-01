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

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

/**
 * @since 1.3
 */
class SingleLockTests {

	@Test
	@SuppressWarnings("resource")
	void acquire() throws Exception {
		var lock = new ReentrantLock();

		new SingleLock(lock).acquire();

		assertTrue(lock.isLocked());
	}

	@Test
	@SuppressWarnings("resource")
	void release() throws Exception {
		var lock = new ReentrantLock();

		new SingleLock(lock).acquire().close();

		assertFalse(lock.isLocked());
	}

}
