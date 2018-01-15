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

interface ResourceLock extends AutoCloseable {

	// TODO: Maybe introduce a specialized type that implements AutoClosable instead of ResourceLock itself
	ResourceLock acquire() throws InterruptedException;

	void release();

	@Override
	default void close() {
		release();
	}
}
