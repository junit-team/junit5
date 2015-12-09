package org.junit.gen5.engine.junit5ext.descriptor;

public class ClassDescriptor extends GroupDescriptor {
    private final Class<?> testClass;

    public ClassDescriptor(Class<?> testClass, String uniqueId, String displayName) {
        super(uniqueId, displayName);
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
