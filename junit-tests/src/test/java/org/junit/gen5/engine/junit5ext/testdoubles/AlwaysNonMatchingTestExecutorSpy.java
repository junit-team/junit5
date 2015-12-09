
package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistry;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class AlwaysNonMatchingTestExecutorSpy implements TestExecutor {
	public TestExecutorRegistry foundTestExecutorRegistry;
	public TestDescriptor foundTestDescriptor;
	public ExecutionRequest foundExecutionRequest;
	public TestDescriptor foundTestDescriptorForExecution;

	@Override
	public void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry) {
		foundTestExecutorRegistry = testExecutorRegistry;
	}

	@Override
	public boolean canExecute(TestDescriptor testDescriptor) {
		foundTestDescriptor = testDescriptor;
		return false;
	}

	@Override
	public void execute(ExecutionRequest request, TestDescriptor testDescriptor) throws TestSkippedException,
			TestAbortedException, AssertionError {
		foundExecutionRequest = request;
		foundTestDescriptorForExecution = testDescriptor;
	}
}
