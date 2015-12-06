package org.junit.gen5.engine.junit5ext.testable;

public class ClassTestGroup extends TestGroup {
    private final Class<?> testClass;

    public ClassTestGroup(Class<?> testClass, String uniqueId, String displayName) {
        super(uniqueId, displayName);
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
