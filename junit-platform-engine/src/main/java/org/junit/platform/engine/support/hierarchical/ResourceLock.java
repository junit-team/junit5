/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * A lock for a one or more resources.
 *
 * @see HierarchicalTestExecutorService.TestTask#getResourceLock()
 *
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public interface ResourceLock extends AutoCloseable {

	/**
	 * Acquire this resource lock, potentially blocking.
	 *
	 * @return this lock so it can easily be used in a try-with-resources
	 * statement.
	 * @throws InterruptedException if the calling thread is interrupted
	 * while waiting to acquire this lock
	 */
	ResourceLock acquire() throws InterruptedException;

	/**
	 * Release this resource lock.
	 */
	void release();

	@Override
	default void close() {
		release();
	}

}
