/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

// tag::imports[]
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaPackage;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
// end::imports[]

/**
 * @since 5.0
 */
class UsingTheLauncherDemo {

	@Test
	@SuppressWarnings("unused")
	void discovery() {
		// @formatter:off
		// tag::discovery[]
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(
				selectJavaPackage("com.example.mytests"),
				selectJavaClass(MyTestClass.class)
			)
			.filters(includeClassNamePatterns(".*Test"))
			.build();

		TestPlan plan = LauncherFactory.create().discover(request);
		// end::discovery[]
		// @formatter:on
	}

	@Test
	void execution() {
		// @formatter:off
		// tag::execution[]
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(
				selectJavaPackage("com.example.mytests"),
				selectJavaClass(MyTestClass.class)
			)
			.filters(includeClassNamePatterns(".*Test"))
			.build();

		Launcher launcher = LauncherFactory.create();

		// Register a listener of your choice
		TestExecutionListener listener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(listener);

		launcher.execute(request);
		// end::execution[]
		// @formatter:on
	}
}

class MyTestClass {

}
