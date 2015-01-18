package org.junit.lambda.proposal02;

public class ClassContext extends AbstractContext {
    public ClassContext(Class testContextClass, TestContext parent) {
        super(testContextClass.getName(), parent);
    }
}
