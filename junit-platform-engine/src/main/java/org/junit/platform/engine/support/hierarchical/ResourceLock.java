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
