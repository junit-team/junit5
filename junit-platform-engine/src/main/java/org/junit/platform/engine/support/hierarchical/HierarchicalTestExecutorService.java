package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.Future;

import org.junit.platform.engine.TestDescriptor;

public interface HierarchicalTestExecutorService<C extends EngineExecutionContext> extends AutoCloseable {

    Future<Void> submit(TestTask<C> testTask);

    interface TestTask<C extends EngineExecutionContext> {

        C getParentExecutionContext();

        TestDescriptor getTestDescriptor();

        void execute();

    }
}
