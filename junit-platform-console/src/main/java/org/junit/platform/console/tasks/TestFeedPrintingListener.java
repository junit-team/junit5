package org.junit.platform.console.tasks;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.PrintWriter;

import static org.junit.platform.console.tasks.Color.NONE;

public class TestFeedPrintingListener implements TestExecutionListener {

    private final PrintWriter out;
    private final boolean disableAnsiColors;

    public TestFeedPrintingListener(PrintWriter out, boolean disableAnsiColors) {
        this.out = out;
        this.disableAnsiColors = disableAnsiColors;
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        String msg = generateMessageFromUniqueId(testIdentifier.getUniqueId());
        if (testIdentifier.getParentId().isPresent())
            msg += " > " + testIdentifier.getParentId().get();
        msg += " > " + testIdentifier.getDisplayName();
        println(Color.DYNAMIC, "%s \n %s", msg, reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        String msg = generateMessageFromUniqueId(testIdentifier.getUniqueId());
        if (testIdentifier.getParentId().isPresent())
            msg += " > " + testIdentifier.getParentId().get();
        msg += " > " + testIdentifier.getDisplayName();
        println(Color.DYNAMIC, "%s STARTED", msg);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = testExecutionResult.getStatus();
        String msg = generateMessageFromUniqueId(testIdentifier.getUniqueId());
        if (testIdentifier.getParentId().isPresent())
            msg += " > " + testIdentifier.getParentId().get();
        msg += " > " + testIdentifier.getDisplayName();
        if (testExecutionResult.getThrowable().isPresent()) {
            println(Color.DYNAMIC, "%s > %s\n %s", msg, status.toString(),
                    testExecutionResult.getThrowable().get().getMessage());
        } else {
            println(Color.DYNAMIC, "%s > %s", msg, status.toString());
        }
    }

    private void println(Color color, String format, Object... args) {
        println(color, String.format(format, args));
    }

    private void println(Color color, String message) {
        if (this.disableAnsiColors) {
            this.out.println(message);
        }
        else {
            // Use string concatenation to avoid ANSI disruption on console
            this.out.println(color + message + NONE);
        }
    }

    private String generateMessageFromUniqueId(String uniqueId) {
        String[] messages = uniqueId.split("/");
        String engine = parseUniqueId(messages[0]);
        String output = engine + "";
        if (messages.length < 2)
            return output;
        for (int i = 1; i < messages.length - 1; i++) {
            output += " > " + parseUniqueId(messages[i]);
        }
        return output;
    }

    private String parseUniqueId(String uniqueId) {
        if (uniqueId.contains("junit-jupiter")) {
            return "JUnit Jupiter";
        } else if (uniqueId.contains("junit-vintage")) {
            return "JUnit Vintage";
        }
        return parseClassName(uniqueId);
    }

    private String parseClassName(String className) {
        String parsedClassName = className.replaceAll("\\[", "")
                .replaceAll("]", "").replaceAll("class:", "");
        return parsedClassName;
    }
}