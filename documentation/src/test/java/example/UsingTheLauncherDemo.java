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

import static org.junit.gen5.engine.discovery.ClassFilter.byNamePattern;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;

import org.junit.gen5.api.Test;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.listeners.SummaryGeneratingListener;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;
// end::imports[]

class UsingTheLauncherDemo {

	@Test
	void discovery() {
		// tag::discovery[]
		TestDiscoveryRequest specification = TestDiscoveryRequestBuilder.request() //
		.select( //
			forPackageName("com.mycompany.mytests"), //
			forClass(MyTestClass.class) //
		) //
		.filter(byNamePattern(".*Test")) //
		.build();

		TestPlan plan = LauncherFactory.create().discover(specification);
		// end::discovery[]
	}

	@Test
	void execution() {
		// tag::execution[]
		TestDiscoveryRequest specification = TestDiscoveryRequestBuilder.request() //
		.select( //
			forPackageName("com.mycompany.mytests"), //
			forClass(MyTestClass.class) //
		) //
		.filter(byNamePattern(".*Test")) //
		.build();

		Launcher launcher = LauncherFactory.create();

		// Register a listener of your choice
		TestExecutionListener listener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(listener);

		launcher.execute(specification);
		// end::execution[]
	}
}

class MyTestClass {

}
