package org.junit.lambda.proposal02;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContext implements TestContext {

    private String name;
    private List<TestComponentDecorator> decorators = new ArrayList<TestComponentDecorator>();
    private TestComponent parent;

    protected AbstractContext(String name, TestComponent parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TestComponentDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(TestComponentDecorator decorator) {
        decorators.add(decorator);
    }

    @Override
    public TestComponent getParent() {
        return parent;
    }

}
