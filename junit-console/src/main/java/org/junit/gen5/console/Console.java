package org.junit.gen5.console;

import org.junit.gen5.engine.TestPlan;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestExecutionPlan;

public class Console {

	public static void main(String[] args) throws Throwable {
		String className = args[0];

		// TODO Configure launcher?
		Launcher launcher = new Launcher();
		
		TestPlan testPlan = TestPlan.builder().classes(Class.forName(className)).build();

		// TODO Launch parameters: Provide configuration
		TestExecutionPlan executionPlan = launcher.discoverTests(testPlan);

		// TODO Register test listener(s)
//        .withTestListener(new ColoredPrintingTestListener(System.out))
//        .withTestListener(new TestSummaryReportingTestListener(System.out))

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(executionPlan);

	}

}
