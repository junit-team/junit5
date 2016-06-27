/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * @since 1.0
 */
public class JUnitPlatformProvider extends AbstractProvider {

	private final ProviderParameters parameters;

	public JUnitPlatformProvider(ProviderParameters parameters) {
		this.parameters = parameters;
		Logger.getLogger("org.junit").setLevel(Level.WARNING);
	}

	@Override
	public Iterable<Class<?>> getSuites() {
		// TODO Implement suite support in the JUnitPlatformProvider.
		throw new UnsupportedOperationException("Forking is not yet supported.");
	}

	@Override
	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		if (forkTestSet != null) {
			// TODO Implement forking support in the JUnitPlatformProvider.
			throw new UnsupportedOperationException("Forking is not yet supported.");
		}

		Launcher launcher = LauncherFactory.create();
		TestsToRun testsToRun = scanClasspath(launcher);
		return invokeAllTests(testsToRun, launcher);
	}

	private TestsToRun scanClasspath(Launcher launcher) {
		TestsToRun scannedClasses = parameters.getScanResult().applyFilter(new TestPlanScannerFilter(launcher),
			parameters.getTestClassLoader());
		return parameters.getRunOrderCalculator().orderTestClasses(scannedClasses);
	}

	private RunResult invokeAllTests(TestsToRun testsToRun, Launcher launcher) {
		RunResult runResult;
		ReporterFactory reporterFactory = parameters.getReporterFactory();
		try {
			RunListener runListener = reporterFactory.createReporter();
			launcher.registerTestExecutionListeners(new RunListenerAdapter(runListener));

			for (Class<?> testClass : testsToRun) {
				invokeSingleClass(testClass, launcher, runListener);
			}
		}
		finally {
			runResult = reporterFactory.close();
		}
		return runResult;
	}

	private void invokeSingleClass(Class<?> testClass, Launcher launcher, RunListener runListener) {
		SimpleReportEntry classEntry = new SimpleReportEntry(getClass().getName(), testClass.getName());
		runListener.testSetStarting(classEntry);

		TestDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		launcher.execute(discoveryRequest);

		runListener.testSetCompleted(classEntry);
	}

}
