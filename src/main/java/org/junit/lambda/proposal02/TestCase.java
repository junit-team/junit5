package org.junit.lambda.proposal02;

import junit.framework.TestResult;

public abstract class TestCase {

    public abstract TestContext getTestContext();

    public TestResult run() {
        return new TestResult();
    }
}
