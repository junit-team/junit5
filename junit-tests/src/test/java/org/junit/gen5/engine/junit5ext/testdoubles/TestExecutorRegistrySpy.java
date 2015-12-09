
package org.junit.gen5.engine.junit5ext.testdoubles;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistry;

public class TestExecutorRegistrySpy implements TestExecutorRegistry {
	public final static TestExecutor TEST_EXECUTOR = new AlwaysMatchingTestExecutorSpy();

	public List<TestDescriptor> testDescriptors = new LinkedList<>();

	@Override
	public void register(TestExecutor testExecutor) {
	}

	@Override
	public void executeAll(ExecutionRequest request, TestDescriptor testDescriptor) {
		testDescriptors.add(testDescriptor);
	}
}
