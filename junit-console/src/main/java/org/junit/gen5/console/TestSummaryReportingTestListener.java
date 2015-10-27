package org.junit.gen5.console;

import java.io.PrintStream;

import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestListener;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestSummaryReportingTestListener implements TestListener {
	private final PrintStream out;

	int testsFound;
	int testsSkipped;
	int testsAborted;
	int testsSucceeded;
	int testsFailed;

	private long timeStarted;
	private long timePaused;
	private long timeFinished;

	public TestSummaryReportingTestListener(PrintStream out) {
		this.out = out;
	}

	private void reportSummary(String msg) {
		timeFinished = System.currentTimeMillis();

		out.println(String.format(
				"%s after %d ms\n" + "[%10d tests found     ]\n" + "[%10d tests skipped   ]\n"
						+ "[%10d tests aborted   ]\n" + "[%10d tests failed    ]\n" + "[%10d tests successful]\n",
				msg, timeFinished - timeStarted, testsFound, testsSkipped, testsAborted, testsFailed, testsSucceeded));
	}

	@Override
	public void testFound(TestIdentifier testIdentifier) {
		testsFound++;
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
		testsSkipped++;
	}

	@Override
	public void testAborted(TestIdentifier testIdentifier, Throwable t) {
		testsAborted++;
	}

	@Override
	public void testFailed(TestIdentifier testIdentifier, Throwable t) {
		testsFailed++;
	}

	@Override
	public void testSucceeded(TestIdentifier testIdentifier) {
		testsSucceeded++;
	}
}