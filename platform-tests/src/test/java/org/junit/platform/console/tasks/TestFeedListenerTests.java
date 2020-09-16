package org.junit.platform.console.tasks;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class TestFeedListenerTests {

    private static final String EOL = System.lineSeparator();

    @Test
    public void testExecutionSkipped() {
        StringWriter stringWriter = new StringWriter();
        listener(stringWriter).executionSkipped(newTestIdentifier(), "Test disabled");
        String[] lines = lines(stringWriter);
        assertLinesMatch(List.of("[engine:demo-engine] > %c ool test SKIPPED\n Test disabled"), List.of(lines));
    }

    @Test
    public void testExecutionFailed() {
        StringWriter stringWriter = new StringWriter();
        listener(stringWriter).executionFinished(newTestIdentifier(),
                TestExecutionResult.failed(new AssertionError("Boom!")));
        String[] lines = lines(stringWriter);
        assertLinesMatch(List.of("[engine:demo-engine] > %c ool test > FAILED \n Boom!"), List.of(lines));
    }

    @Test
    public void testExecutionSucceeded() {
        StringWriter stringWriter = new StringWriter();
        listener(stringWriter).executionFinished(newTestIdentifier(), TestExecutionResult.successful());
        String[] lines = lines(stringWriter);
        assertLinesMatch(List.of("[engine:demo-engine] > %c ool test > " +
                TestExecutionResult.Status.SUCCESSFUL.toString()), List.of(lines));
    }

    private TestFeedPrintingListener listener(StringWriter stringWriter) {
        return new TestFeedPrintingListener(new PrintWriter(stringWriter), true);
    }

    private static TestIdentifier newTestIdentifier() {
        TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "%c ool test");
        return TestIdentifier.from(testDescriptor);
    }

    private String[] lines(StringWriter stringWriter) {
        return stringWriter.toString().split(EOL);
    }

}
