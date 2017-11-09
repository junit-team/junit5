package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.Future;

import org.junit.platform.engine.TestDescriptor;

public class SameThreadTestDescriptorExecutorService implements TestDescriptorExecutorService {

    @Override
    public Future<Void> submit(TestDescriptor testDescriptor, TestExecution testExecution) {
        testExecution.execute(testDescriptor);
        return completedFuture(null);
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

}
