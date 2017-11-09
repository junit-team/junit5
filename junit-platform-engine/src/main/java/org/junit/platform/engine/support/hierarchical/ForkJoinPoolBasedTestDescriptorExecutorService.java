package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.junit.platform.engine.TestDescriptor;

public class ForkJoinPoolBasedTestDescriptorExecutorService implements TestDescriptorExecutorService {

    private final ExecutorService forkJoinPool;

    public ForkJoinPoolBasedTestDescriptorExecutorService() {
        forkJoinPool = new ForkJoinPool();
    }

    @Override
    public Future<Void> submit(TestDescriptor testDescriptor, TestExecution testExecution) {
        return forkJoinPool.submit(() -> {
            testExecution.execute(testDescriptor);
            return null;
        });
    }

    @Override
    public void close() throws Exception {
        forkJoinPool.shutdownNow();
    }

}
