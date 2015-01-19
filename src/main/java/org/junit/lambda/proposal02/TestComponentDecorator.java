package org.junit.lambda.proposal02;

import junit.framework.TestResult;

/**
 * As it's currently "implemented" TestComponentDecorator does not follow the GOF decorator pattern.
 * Instead it's a combination of Decorator factory - using the build method - and an interceptor - when
 * using the run method.
 */

public interface TestComponentDecorator {

    default TestComponent build(TestComponent testComponent) {
        return testComponent;
    }

    default public TestResult run(TestComponent testComponent)  throws Exception {
        return testComponent.run();
    }

}
