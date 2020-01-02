/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::imports[]
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;
// end::imports[]

/**
 * @since 5.0
 */
class UsingTheLauncherDemo {

	@org.junit.jupiter.api.Test
	@SuppressWarnings("unused")
	void discovery() {
		// @formatter:off
		// tag::discovery[]
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(
				selectPackage("com.example.mytests"),
				selectClass(MyTestClass.class)
			)
			.filters(
				includeClassNamePatterns(".*Tests")
			)
			.build();

		Launcher launcher = LauncherFactory.create();

		TestPlan testPlan = launcher.discover(request);
		// end::discovery[]
		// @formatter:on
	}

	@org.junit.jupiter.api.Test
	@SuppressWarnings("unused")
	void execution() {
		// @formatter:off
		// tag::execution[]
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(
				selectPackage("com.example.mytests"),
				selectClass(MyTestClass.class)
			)
			.filters(
				includeClassNamePatterns(".*Tests")
			)
			.build();

		Launcher launcher = LauncherFactory.create();

		// Register a listener of your choice
		SummaryGeneratingListener listener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(listener);

		launcher.execute(request);

		TestExecutionSummary summary = listener.getSummary();
		// Do something with the TestExecutionSummary.

		// end::execution[]
		// @formatter:on
	}

	@org.junit.jupiter.api.Test
	void launcherConfig() {
		Path reportsDir = Paths.get("target", "xml-reports");
		PrintWriter out = new PrintWriter(System.out);
		// @formatter:off
		// tag::launcherConfig[]
		LauncherConfig launcherConfig = LauncherConfig.builder()
			.enableTestEngineAutoRegistration(false)
			.enableTestExecutionListenerAutoRegistration(false)
			.addTestEngines(new CustomTestEngine())
			.addTestExecutionListeners(new LegacyXmlReportGeneratingListener(reportsDir, out))
			.addTestExecutionListeners(new CustomTestExecutionListener())
			.build();

		Launcher launcher = LauncherFactory.create(launcherConfig);

		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(selectPackage("com.example.mytests"))
			.build();

		launcher.execute(request);
		// end::launcherConfig[]
		// @formatter:on
	}

}

class MyTestClass {
}

class CustomTestExecutionListener implements TestExecutionListener {
}
