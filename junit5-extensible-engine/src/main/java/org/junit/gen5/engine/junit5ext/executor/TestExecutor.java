
package org.junit.gen5.engine.junit5ext.executor;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public interface TestExecutor {
	void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry);

	boolean canExecute(TestDescriptor testDescriptor);

	void execute(ExecutionRequest request, TestDescriptor testDescriptor)
            throws TestSkippedException, TestAbortedException, AssertionError;
}