package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestExecutorRegistrySpy implements TestExecutorRegistry {
    public final static TestExecutor TEST_EXECUTOR = new AlwaysMatchingTestExecutorStub();

    public List<TestDescriptor> testDescriptors = new LinkedList<>();

    @Override
    public List<TestExecutor> lookupExecutors(TestDescriptor testDescriptor) {
        testDescriptors.add(testDescriptor);
        return Collections.singletonList(TEST_EXECUTOR);
    }

    @Override
    public void register(TestExecutor testExecutor) {
    }
}
