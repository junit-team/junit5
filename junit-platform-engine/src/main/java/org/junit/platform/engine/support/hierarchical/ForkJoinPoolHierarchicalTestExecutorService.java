package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ForkJoinPoolHierarchicalTestExecutorService<C extends EngineExecutionContext> implements HierarchicalTestExecutorService<C> {

    private final ForkJoinPool forkJoinPool;

    public ForkJoinPoolHierarchicalTestExecutorService() {
        forkJoinPool = new ForkJoinPool();
    }

    @Override
    public Future<Void> submit(TestTask<C> testTask) {
        return forkJoinPool.submit(() -> {
            testTask.execute();
            return null;
        });
    }

    @Override
    public void close() throws Exception {
        forkJoinPool.shutdownNow();
    }

}
