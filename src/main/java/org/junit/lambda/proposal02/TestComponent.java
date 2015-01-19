package org.junit.lambda.proposal02;

import junit.framework.TestResult;

import java.util.List;

public interface TestComponent {
    void build(TestComponent parent);
    String getName();
    TestComponent getParent();
    List<TestComponentDecorator> getDecorators();
    TestResult run();
}
