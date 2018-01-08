package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.Callable;

class ExclusiveTask<V> implements Callable<V>{

	private final ResourceLock resourceLock;
	private final Callable<V> delegate;

	ExclusiveTask(ResourceLock resourceLock, Callable<V> delegate) {
		this.resourceLock = resourceLock;
		this.delegate = delegate;
	}

	@Override
	public V call() throws Exception {
		try(ResourceLock lock = resourceLock.acquire()) {
			return delegate.call();
		}
	}
}
