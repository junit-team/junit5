
package org.junit.gen5.console;

import org.junit.gen5.engine.TestListenerRegistry;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class ConsoleRunner {

	public static void main(String[] args) throws Throwable {

		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		ColoredPrintingTestListener printingListener = new ColoredPrintingTestListener(System.out);
		TestSummaryReportingTestListener reportingListener = new TestSummaryReportingTestListener(System.out);

		TestListenerRegistry.registerTestExecutionListener(printingListener);
		TestListenerRegistry.registerTestExecutionListener(reportingListener);

		launcher.registerTestPlanExecutionListener(printingListener);
		launcher.registerTestPlanExecutionListener(reportingListener);

		TestPlanSpecification testPlanSpecification = TestPlanSpecification.builder().classNames(args).build();

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlanSpecification);
	}

}