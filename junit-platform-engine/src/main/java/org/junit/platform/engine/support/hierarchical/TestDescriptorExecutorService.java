package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.Future;

import org.junit.platform.engine.TestDescriptor;

public interface TestDescriptorExecutorService extends AutoCloseable {

    Future<Void> submit(TestDescriptor testDescriptor, TestExecution testExecution);

    interface TestExecution {
        void execute(TestDescriptor testDescriptor);
    }
}
