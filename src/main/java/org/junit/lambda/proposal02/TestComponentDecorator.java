package org.junit.lambda.proposal02;

import junit.framework.TestResult;

public interface TestComponentDecorator {

    default TestComponent build(TestComponent testComponent) {
        return testComponent;
    }

    default public TestResult run(TestComponent testComponent)  throws Exception {
        return testComponent.run();
    }

}
