
package org.junit.gen5.engine.junit5ext.executor;

import java.util.List;

import org.junit.gen5.engine.TestDescriptor;

public interface TestExecutorRegistry {
	List<TestExecutor> lookupExecutors(TestDescriptor testDescriptor);

	void register(TestExecutor testExecutor);
}
