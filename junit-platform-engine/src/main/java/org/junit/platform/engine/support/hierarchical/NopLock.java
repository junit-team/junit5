package org.junit.platform.engine.support.hierarchical;

public class NopLock implements ResourceLock {
	public static final ResourceLock INSTANCE = new NopLock();

	private NopLock() {
	}

	@Override
	public ResourceLock acquire() {
		return this;
	}

	@Override
	public void release() {

	}
}
