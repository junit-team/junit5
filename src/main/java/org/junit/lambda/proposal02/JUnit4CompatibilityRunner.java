package org.junit.lambda.proposal02;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * Add this runner to run JUnit Lambda style tests with IDEs that do not support JUnit lambda
 */
public class JUnit4CompatibilityRunner extends Runner {
    @Override
    public Description getDescription() {
        return null;
    }

    @Override
    public void run(RunNotifier runNotifier) {

    }
}
