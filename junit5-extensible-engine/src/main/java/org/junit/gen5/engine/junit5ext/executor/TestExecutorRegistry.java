
package org.junit.gen5.engine.junit5ext.executor;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;

public interface TestExecutorRegistry {
	void register(TestExecutor testExecutor);

	void executeAll(ExecutionRequest request, TestDescriptor testDescriptor);
}
