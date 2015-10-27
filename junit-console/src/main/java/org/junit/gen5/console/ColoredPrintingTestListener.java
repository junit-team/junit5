package org.junit.gen5.console;

import java.io.PrintStream;

import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestListener;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ColoredPrintingTestListener implements TestListener {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private final PrintStream out;

	public ColoredPrintingTestListener(PrintStream out) {
		this.out = out;
	}

	@Override
	public void testFound(TestIdentifier testIdentifier) {
		out.print(ANSI_GREEN);
		out.format("Test found:     %s", testIdentifier.toString());
		out.println(ANSI_RESET);
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		out.print(ANSI_GREEN);
		out.format("Test started:   %s", testIdentifier.toString());
		out.println(ANSI_RESET);
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
		out.print(ANSI_YELLOW);
		out.format("Test skipped:   %s\n=> Exception:   %s", testIdentifier.toString(),
				(t != null) ? t.getLocalizedMessage() : "none");
		out.println(ANSI_RESET);
	}

	@Override
	public void testAborted(TestIdentifier testIdentifier, Throwable t) {
		out.print(ANSI_YELLOW);
		out.format("Test aborted:   %s\n=> Exception:   %s", testIdentifier.toString(),
				(t != null) ? t.getLocalizedMessage() : "none");
		out.println(ANSI_RESET);
	}

	@Override
	public void testFailed(TestIdentifier testIdentifier, Throwable t) {
		out.print(ANSI_RED);
		out.format("Test failed:    %s\n=> Exception:   %s", testIdentifier.toString(), t.getLocalizedMessage());
		out.println(ANSI_RESET);
	}

	@Override
	public void testSucceeded(TestIdentifier testIdentifier) {
		out.print(ANSI_GREEN);
		out.format("Test succeeded: %s", testIdentifier.toString());
		out.println(ANSI_RESET);
	}
}
