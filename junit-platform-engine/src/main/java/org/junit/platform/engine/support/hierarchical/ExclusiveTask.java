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

import java.util.concurrent.Callable;

class ExclusiveTask<V> implements Callable<V> {

	private final ResourceLock resourceLock;
	private final Callable<V> delegate;

	ExclusiveTask(ResourceLock resourceLock, Callable<V> delegate) {
		this.resourceLock = resourceLock;
		this.delegate = delegate;
	}

	@Override
	public V call() throws Exception {
		try {
			resourceLock.acquire();
			return delegate.call();
		}
		finally {
			resourceLock.close();
		}
	}
}
