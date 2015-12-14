package org.junit.gen5.engine.junit5.resolver;

import org.junit.gen5.engine.TestEngine;

public abstract class JUnit5TestResolver implements TestResolver {
    private TestEngine testEngine;

    @Override
    public void setTestEngine(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    public TestEngine getTestEngine() {
        return testEngine;
    }
}
