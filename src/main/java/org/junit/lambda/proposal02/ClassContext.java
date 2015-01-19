package org.junit.lambda.proposal02;

public class ClassContext extends AbstractContext {
    public ClassContext(Class testContextClass, TestComponent parent) {
        super(testContextClass.getName(), parent);
    }
}
