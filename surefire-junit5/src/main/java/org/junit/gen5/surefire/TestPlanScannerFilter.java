/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.surefire;

import static org.junit.gen5.engine.TestPlanSpecification.build;

import org.apache.maven.surefire.util.ScannerFilter;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

final class TestPlanScannerFilter implements ScannerFilter {

	private final Launcher launcher;

	public TestPlanScannerFilter(Launcher launcher) {
		this.launcher = launcher;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean accept(Class testClass) {
		TestPlanSpecification specification = build(TestPlanSpecification.forClass(testClass));
		TestPlan testPlan = launcher.discover(specification);
		return testPlan.countTestIdentifiers(TestIdentifier::isTest) > 0;
	}
}