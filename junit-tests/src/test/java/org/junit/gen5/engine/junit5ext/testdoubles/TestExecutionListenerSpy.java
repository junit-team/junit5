package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

import java.util.LinkedList;
import java.util.List;

public class TestExecutionListenerSpy implements TestExecutionListener {
    public List<TestDescriptor> foundDynamicTests = new LinkedList<>();
    public List<TestDescriptor> foundStartedTests = new LinkedList<>();
    public List<TestDescriptor> foundSkippedTests = new LinkedList<>();
    public List<TestDescriptor> foundAbortedTests = new LinkedList<>();
    public List<TestDescriptor> foundFailedTests = new LinkedList<>();
    public List<TestDescriptor> foundSucceededTests = new LinkedList<>();

    @Override
    public void dynamicTestFound(TestDescriptor testDescriptor) {
        foundDynamicTests.add(testDescriptor);
    }

    @Override
    public void testStarted(TestDescriptor testDescriptor) {
        foundStartedTests.add(testDescriptor);
    }

    @Override
    public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
        foundSkippedTests.add(testDescriptor);
    }

    @Override
    public void testAborted(TestDescriptor testDescriptor, Throwable t) {
        foundAbortedTests.add(testDescriptor);
    }

    @Override
    public void testFailed(TestDescriptor testDescriptor, Throwable t) {
        foundFailedTests.add(testDescriptor);
    }

    @Override
    public void testSucceeded(TestDescriptor testDescriptor) {
        foundSucceededTests.add(testDescriptor);
    }
}
