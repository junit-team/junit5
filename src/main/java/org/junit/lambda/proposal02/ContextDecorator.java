package org.junit.lambda.proposal02;

import junit.framework.TestResult;

import java.io.IOException;

public interface ContextDecorator {

    default public void before() throws IOException {

    }

    default public void after() throws IOException {

    }

    default public TestResult runTest(TestCase testCase) {
        return testCase.run();
    }

    default public void beforeAll(TestContext context) throws IOException {

    }

    default public void afterAll(TestContext context) throws IOException {

    }
}
