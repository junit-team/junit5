package org.junit.lambda.proposal02;

import java.util.List;

public interface TestContext {
    String getName();
    TestContext getParent();
    List<ContextDecorator> getDecorators();
}
