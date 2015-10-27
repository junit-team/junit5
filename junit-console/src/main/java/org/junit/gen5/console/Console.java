package org.junit.gen5.console;

import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestExecutionPlan;

public class Console {

	public static void main(String[] args) throws Exception {
		String className = args[0];

		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		// TODO Launch parameters: Provide configuration
		TestExecutionPlan testPlan = launcher.discoverTests(className);

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlan);
	}

}
