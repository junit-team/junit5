package org.junit.lambda.proposal02;

import junit.framework.TestResult;

import java.io.IOException;

public interface TestDecorator {

    default public void before() throws Exception {

    }

    default public void after() throws Exception {

    }

    default public TestResult runTest(TestCase testCase)  throws Exception {
        return testCase.run();
    }

    default public void beforeAll(TestContext context) throws Exception {

    }

    default public void afterAll(TestContext context) throws Exception {

    }
}
