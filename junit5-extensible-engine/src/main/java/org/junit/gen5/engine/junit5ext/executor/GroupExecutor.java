
package org.junit.gen5.engine.junit5ext.executor;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class GroupExecutor implements TestExecutor {
	private TestExecutorRegistry testExecutorRegistry;

	@Override
	public void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry) {
		this.testExecutorRegistry = testExecutorRegistry;
	}

	@Override
	public boolean canExecute(TestDescriptor testDescriptor) {
		return testDescriptor instanceof GroupDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request, TestDescriptor testDescriptor) throws TestSkippedException,
			TestAbortedException, AssertionError {
		GroupDescriptor groupDescriptor = (GroupDescriptor) testDescriptor;
		groupDescriptor.getChildren().forEach(child -> testExecutorRegistry.executeAll(request, child));
	}
}
