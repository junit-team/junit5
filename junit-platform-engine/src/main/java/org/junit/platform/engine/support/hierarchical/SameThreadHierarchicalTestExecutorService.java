package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.Future;

public class SameThreadHierarchicalTestExecutorService<C extends EngineExecutionContext> implements HierarchicalTestExecutorService<C> {

    @Override
    public Future<Void> submit(TestTask<C> testTask) {
        testTask.execute();
        return completedFuture(null);
    }

    @Override
    public void close() {
        // nothing to do
    }

}
