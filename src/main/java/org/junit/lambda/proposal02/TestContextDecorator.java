package org.junit.lambda.proposal02;

import junit.framework.TestResult;

public interface TestContextDecorator extends TestComponentDecorator {


    default public TestResult runChild(TestComponent testComponent)  throws Exception {
        return testComponent.run();
    }



}
