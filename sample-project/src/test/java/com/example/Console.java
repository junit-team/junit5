
package com.example;

import org.junit.gen5.console.ColoredPrintingTestListener;
import org.junit.gen5.console.TestSummaryReportingTestListener;
import org.junit.gen5.engine.TestListenerRegistry;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestPlan;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class Console {

	public static void main(String[] args) throws Throwable {

		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		ColoredPrintingTestListener printingListener = new ColoredPrintingTestListener(System.out);
		TestSummaryReportingTestListener reportingListener = new TestSummaryReportingTestListener(System.out);

		TestListenerRegistry.registerTestExecutionListener(printingListener);
		TestListenerRegistry.registerTestExecutionListener(reportingListener);

		launcher.registerTestPlanExecutionListener(printingListener);
		launcher.registerTestPlanExecutionListener(reportingListener);

		TestPlanSpecification testPlanConfiguration = TestPlanSpecification.builder().classNames(args).build();

		// TODO Launch parameters: Provide configuration
		TestPlan testPlan = launcher.createTestPlanWithConfiguration(testPlanConfiguration);

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlan);
	}

}