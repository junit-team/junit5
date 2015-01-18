package org.junit.lambda.proposal02;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContext implements TestContext {

    private String name;
    private List<TestDecorator> decorators = new ArrayList<TestDecorator>();
    private TestContext parent;

    protected AbstractContext(String name, TestContext parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TestDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(TestDecorator decorator) {
        decorators.add(decorator);
    }

    @Override
    public TestContext getParent() {
        return parent;
    }

}
