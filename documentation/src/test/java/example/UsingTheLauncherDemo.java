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
import static org.junit.gen5.engine.discovery.ClassFilter.includeClassNamePattern;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.engine.discovery.PackageSelector.selectPackage;

import org.junit.gen5.api.Test;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.listeners.SummaryGeneratingListener;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;
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
		TestDiscoveryRequest specification = TestDiscoveryRequestBuilder.request()
			.selectors(
				selectPackage("com.example.mytests"),
				selectClass(MyTestClass.class)
			)
			.filters(includeClassNamePattern(".*Test"))
			.build();

		TestPlan plan = LauncherFactory.create().discover(specification);
		// end::discovery[]
		// @formatter:on
	}

	@Test
	void execution() {
		// @formatter:off
		// tag::execution[]
		TestDiscoveryRequest specification = TestDiscoveryRequestBuilder.request()
			.selectors(
				selectPackage("com.example.mytests"),
				selectClass(MyTestClass.class)
			)
			.filters(includeClassNamePattern(".*Test"))
			.build();

		Launcher launcher = LauncherFactory.create();

		// Register a listener of your choice
		TestExecutionListener listener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(listener);

		launcher.execute(specification);
		// end::execution[]
		// @formatter:on
	}
}

class MyTestClass {

}
