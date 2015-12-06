package org.junit.gen5.engine.junit5ext.testable;

import java.lang.reflect.Method;

public class MethodTest extends TestGroup {
    private final Method method;

    public MethodTest(Method method, String uniqueId, String displayName) {
        super(uniqueId, displayName);
        this.method = method;
    }
}