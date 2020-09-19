package org.junit.platform.console.tasks;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

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
        if (testIdentifier.isContainer())
            return;
        String msg = printTestIdenfifier(testIdentifier);
        println(Color.DYNAMIC, "%s SKIPPED\n %s", msg, reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isContainer())
            return;
        String msg = printTestIdenfifier(testIdentifier);
        println(Color.DYNAMIC, "%s > STARTED", msg);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isContainer())
            return;
        TestExecutionResult.Status status = testExecutionResult.getStatus();
        String msg = printTestIdenfifier(testIdentifier);
        if (testExecutionResult.getThrowable().isPresent()) {
            println(Color.DYNAMIC, "%s > %s \n %s", msg, status.toString(),
                    testExecutionResult.getThrowable().get().getMessage());
        } else {
            println(Color.DYNAMIC, "%s > %s ", msg, status.toString());
        }
    }

    private String printTestIdenfifier (TestIdentifier testIdentifier) {
        String msg = generateMessageFromUniqueId(testIdentifier.getUniqueId());
        msg += " > " + testIdentifier.getDisplayName();
        return msg;
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
        String engine = parseEngine(messages[0]);
        String output = engine + "";
        for (int i = 1; i < messages.length; i++) {
            String message = messages[i];
            if (message.indexOf("class:") != -1) {
                output += " > " + parseMessage(message, "class:");
            } else if (message.indexOf("method:") != -1) {
                output += " > " + parseMessage(message, "method:");
            }
        }
        return output;
    }

    private String parseEngine(String uniqueId) {
        if (uniqueId.contains("junit-jupiter")) {
            return "JUnit Jupiter";
        } else if (uniqueId.contains("junit-vintage")) {
            return "JUnit Vintage";
        }
        return uniqueId;
    }

    private String parseMessage(String className, String prefix) {
        String parsedClassName = className.replaceAll("\\[", "")
                .replaceAll("]", "").replaceAll(prefix, "");
        return parsedClassName;
    }
}