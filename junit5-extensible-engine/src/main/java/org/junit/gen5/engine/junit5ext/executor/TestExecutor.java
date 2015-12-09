package org.junit.gen5.engine.junit5ext.executor;

import org.junit.gen5.engine.TestDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public interface TestExecutor {
    boolean canExecute(TestDescriptor testDescriptor);
    void execute(TestDescriptor testDescriptor) throws TestSkippedException, TestAbortedException, AssertionError;
}