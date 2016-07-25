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
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

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
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * @since 1.0
 */
public class JUnitPlatformProvider extends AbstractProvider {

	private final ProviderParameters parameters;
	private final Launcher launcher;

	public JUnitPlatformProvider(ProviderParameters parameters) {
		this(parameters, LauncherFactory.create());
	}

	JUnitPlatformProvider(ProviderParameters parameters, Launcher launcher) {
		this.parameters = parameters;
		this.launcher = launcher;
		Logger.getLogger("org.junit").setLevel(Level.WARNING);
	}

	@Override
	public Iterable<Class<?>> getSuites() {
		return scanClasspath();
	}

	@Override
	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		if (forkTestSet instanceof TestsToRun) {
			return invokeAllTests((TestsToRun) forkTestSet);
		}
		else if (forkTestSet instanceof Class) {
			return invokeAllTests(TestsToRun.fromClass(((Class<?>) forkTestSet)));
		}
		else if (forkTestSet == null) {
			return invokeAllTests(scanClasspath());
		}
		else {
			throw new IllegalArgumentException("Unexpected value of forkTestSet: " + forkTestSet);
		}
	}

	private TestsToRun scanClasspath() {
		TestsToRun scannedClasses = parameters.getScanResult().applyFilter(new TestPlanScannerFilter(launcher),
			parameters.getTestClassLoader());
		return parameters.getRunOrderCalculator().orderTestClasses(scannedClasses);
	}

	private RunResult invokeAllTests(TestsToRun testsToRun) {
		RunResult runResult;
		ReporterFactory reporterFactory = parameters.getReporterFactory();
		try {
			RunListener runListener = reporterFactory.createReporter();
			launcher.registerTestExecutionListeners(new RunListenerAdapter(runListener));

			for (Class<?> testClass : testsToRun) {
				invokeSingleClass(testClass, runListener);
			}
		}
		finally {
			runResult = reporterFactory.close();
		}
		return runResult;
	}

	private void invokeSingleClass(Class<?> testClass, RunListener runListener) {
		SimpleReportEntry classEntry = new SimpleReportEntry(getClass().getName(), testClass.getName());
		runListener.testSetStarting(classEntry);

		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		launcher.execute(discoveryRequest);

		runListener.testSetCompleted(classEntry);
	}

}
