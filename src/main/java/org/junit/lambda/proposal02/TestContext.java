package org.junit.lambda.proposal02;

import java.util.List;

public interface TestContext extends TestComponent {
    List<TestComponent> getChildren();
}
