package org.junit.gen5.engine.junit5ext.descriptor;

import java.lang.reflect.Method;

public class MethodDescriptor extends GroupDescriptor {
    private final Method method;

    public MethodDescriptor(Method method, String uniqueId, String displayName) {
        super(uniqueId, displayName);
        this.method = method;
    }
}