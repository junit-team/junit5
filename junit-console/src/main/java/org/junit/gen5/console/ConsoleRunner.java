
package org.junit.gen5.console;

import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class ConsoleRunner {

	public static void main(String... args) {

		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		launcher.registerTestPlanExecutionListeners(
			new ColoredPrintingTestListener(System.out),
			new TestSummaryReportingTestListener(System.out));

		TestPlanSpecification testPlanSpecification = TestPlanSpecification.builder().classNames(args).build();

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlanSpecification);
	}

}
